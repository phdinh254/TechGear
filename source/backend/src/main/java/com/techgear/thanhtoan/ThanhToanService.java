package com.techgear.thanhtoan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.loi.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Thanh toán PayOS (https://payos.vn/docs/api/). Hai đường ghi nhận kết quả, dùng chung {@link #ghiKetQua}:
 * webhook PayOS (đường chính khi triển khai) và trang kết quả – backend tự hỏi PayOS, không tin tham số trên returnUrl
 * (PayOS không ký), nhờ vậy chạy ở localhost (webhook không tới được) vẫn ghi nhận được.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThanhToanService {

    /** Liên kết thanh toán hết hạn sau từng này kể từ lúc đặt hàng. */
    public static final Duration HAN_THANH_TOAN = Duration.ofMinutes(30);
    /** PayOS giới hạn mô tả 9 ký tự với tài khoản ngân hàng không liên kết qua PayOS. */
    static final int DO_DAI_MO_TA = 9;

    private final ThanhToanRepository thanhToanRepository;
    private final PayOsProperties payOs;
    private final PayOsClient payOsClient;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    /** {@code trangThai}: cho_xu_ly | thanh_cong | that_bai (trạng thái giao dịch). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record KetQua(boolean hopLe, boolean thanhCong, String trangThai, String maDonHang, String trangThaiThanhToan,
                         String trangThaiDon, boolean coTheThanhToanLai, String thongBao) {
    }

    /** Link vừa tạo kèm tóm tắt cho trang thanh toán (khách vãng lai không xem được chi tiết đơn). */
    public record LinkThanhToan(String duongDan, long orderCode, String maDonHang, long tongTien, OffsetDateTime hetHan) {
    }

    /** Mỗi lần gọi tạo một giao dịch mới: orderCode = id đơn × 100 + số lần thử (1–99). */
    @Transactional
    public LinkThanhToan taoLink(String maDonHang) {
        if (!payOs.daCauHinh()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình PayOS");
        }
        ThanhToanRepository.Don don = thanhToanRepository.khoaDon(maDonHang)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy đơn hàng"));
        if (!"payos".equals(don.phuongThucThanhToan())) {
            throw ApiException.xungDot("Đơn hàng không thanh toán qua PayOS");
        }
        if ("da_thanh_toan".equals(don.trangThaiThanhToan())) {
            throw ApiException.xungDot("Đơn hàng đã được thanh toán");
        }
        if (!"cho_xac_nhan".equals(don.trangThai())) {
            throw ApiException.xungDot("Đơn hàng không còn chờ thanh toán");
        }
        OffsetDateTime hetHan = don.createdAt().plus(HAN_THANH_TOAN);
        if (!OffsetDateTime.now(clock).isBefore(hetHan)) {
            throw ApiException.xungDot("Đơn hàng đã quá hạn thanh toán");
        }
        int lan = thanhToanRepository.soLanThu(don.id()) + 1;
        if (lan > 99) {
            throw ApiException.xungDot("Đơn hàng đã thử thanh toán quá nhiều lần");
        }
        long orderCode = don.id() * 100 + lan;
        thanhToanRepository.themGiaoDich(don.id(), don.tongTien(), String.valueOf(orderCode));
        // Mã đơn (ngẫu nhiên) nằm trong đường dẫn trả về: trang kết quả phải có cả orderCode lẫn mã đơn, nên không dò được
        // orderCode (tuần tự) để lấy thông tin đơn
        String duongDan = payOsClient.taoLink(new PayOsClient.YeuCauLink(orderCode, don.tongTien(), moTa(orderCode),
                payOs.returnUrl() + "/" + don.maDonHang(), payOs.cancelUrl() + "/" + don.maDonHang(),
                hetHan.toEpochSecond())).checkoutUrl();
        return new LinkThanhToan(duongDan, orderCode, don.maDonHang(), don.tongTien(), hetHan);
    }

    /** Webhook: chữ ký → giao dịch → số tiền → còn chờ → ghi. Dữ liệu không khớp thì bỏ qua (vẫn trả 200 cho PayOS). */
    @Transactional
    @SuppressWarnings("unchecked")
    public void xuLyWebhook(Map<String, Object> body) {
        Object d = body.get("data");
        if (!(d instanceof Map<?, ?>)) {
            log.warn("Webhook PayOS không có data, bỏ qua");
            return;
        }
        Map<String, Object> data = (Map<String, Object>) d;
        if (!(body.get("signature") instanceof String chuKy) || !ChuKyPayOs.hopLe(data, chuKy, payOs.checksumKey())) {
            log.warn("Webhook PayOS sai chữ ký, orderCode {}", data.get("orderCode"));
            return;
        }
        String maThamChieu = String.valueOf(data.get("orderCode"));
        ThanhToanRepository.GiaoDich gd = thanhToanRepository.khoaGiaoDich(maThamChieu).orElse(null);
        if (gd == null) {
            log.info("Webhook PayOS cho orderCode {} không có trong hệ thống (có thể là webhook thử), bỏ qua", maThamChieu);
            return;
        }
        if (!(data.get("amount") instanceof Number soTien) || soTien.longValue() != gd.soTien()) {
            log.warn("Webhook PayOS sai số tiền, orderCode {}: {} ≠ {}", maThamChieu, data.get("amount"), gd.soTien());
            return;
        }
        boolean thanhCong = "00".equals(data.get("code"));
        Object reference = data.get("reference");
        ghiKetQua(gd, thanhCong, thanhCong && reference != null ? String.valueOf(reference) : null, body);
    }

    /**
     * Trang kết quả (cần cả orderCode và mã đơn). Giao dịch còn chờ thì hỏi PayOS – ngoài khóa, để PayOS chậm không giữ khóa
     * dòng – rồi khóa và ghi nếu đã có kết quả cuối; giao dịch đã kết thúc thì chỉ đọc.
     */
    @Transactional
    public KetQua ketQua(String orderCode, String maDonHang) {
        if (orderCode == null || !orderCode.matches("\\d{3,18}") || maDonHang == null || maDonHang.isBlank()) {
            return khongHopLe();
        }
        ThanhToanRepository.GiaoDich gd = thanhToanRepository.giaoDich(orderCode).orElse(null);
        if (gd == null || !gd.maDonHang().equalsIgnoreCase(maDonHang.trim())) {
            return khongHopLe();
        }
        if ("cho_xu_ly".equals(gd.trangThai())) {
            PayOsClient.ThongTinLink tt = hoiPayOs(Long.parseLong(orderCode));
            boolean daTra = tt != null && "PAID".equals(tt.status()) && tt.amountPaid() >= gd.soTien();
            boolean thatBai = tt != null && ("CANCELLED".equals(tt.status()) || "EXPIRED".equals(tt.status()));
            if (daTra || thatBai) {
                // ghiKetQua kiểm lại "còn chờ" trên dòng đã khóa (webhook có thể vừa ghi xong)
                ghiKetQua(thanhToanRepository.khoaGiaoDich(orderCode).orElseThrow(), daTra, null, tt);
            }
            gd = thanhToanRepository.giaoDich(orderCode).orElseThrow();
        }
        boolean thanhCong = "thanh_cong".equals(gd.trangThai());
        boolean coTheThanhToanLai = "payos".equals(gd.phuongThucThanhToan())
                && "chua_thanh_toan".equals(gd.trangThaiThanhToan()) && "cho_xac_nhan".equals(gd.trangThaiDon())
                && OffsetDateTime.now(clock).isBefore(gd.ngayDat().plus(HAN_THANH_TOAN));
        String thongBao = switch (gd.trangThai()) {
            case "thanh_cong" -> "Thanh toán thành công";
            case "that_bai" -> "Thanh toán chưa thành công hoặc đã bị hủy";
            default -> "Đang chờ PayOS xác nhận thanh toán";
        };
        return new KetQua(true, thanhCong, gd.trangThai(), gd.maDonHang(), gd.trangThaiThanhToan(), gd.trangThaiDon(),
                coTheThanhToanLai, thongBao);
    }

    /** Ghi kết quả cho giao dịch đã khóa, chỉ khi còn chờ (webhook lặp lại hoặc đến sau trang kết quả thì bỏ qua). */
    private void ghiKetQua(ThanhToanRepository.GiaoDich gd, boolean thanhCong, String maGiaoDich, Object duLieu) {
        if (!"cho_xu_ly".equals(gd.trangThai())) {
            return;
        }
        thanhToanRepository.ketThucGiaoDich(gd.id(), thanhCong, maGiaoDich, jsonMapper.writeValueAsString(duLieu),
                OffsetDateTime.now(clock));
        if (thanhCong) {
            thanhToanRepository.danhDauDaThanhToan(gd.donHangId(), gd.maThamChieu());
        }
    }

    /** PayOS lỗi thì coi như chưa có kết quả (trang kết quả hiện "đang chờ", khách bấm kiểm tra lại). */
    private PayOsClient.ThongTinLink hoiPayOs(long orderCode) {
        try {
            return payOsClient.layThongTin(orderCode);
        } catch (ApiException e) {
            return null;
        }
    }

    static String moTa(long orderCode) {
        String s = "TG" + orderCode;
        return s.length() > DO_DAI_MO_TA ? s.substring(s.length() - DO_DAI_MO_TA) : s;
    }

    private static KetQua khongHopLe() {
        return new KetQua(false, false, null, null, null, null, false, "Không tìm thấy giao dịch thanh toán");
    }
}

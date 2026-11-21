package com.techgear.thanhtoan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.ThoiGian;
import com.techgear.chung.loi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Thanh toán VNPay sandbox 2.1.0 (https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html).
 * Chỉ IPN cập nhật CSDL; trang kết quả chỉ kiểm chữ ký và đọc trạng thái.
 */
@Service
@RequiredArgsConstructor
public class ThanhToanService {

    /** Liên kết thanh toán hết hạn sau từng này kể từ lúc đặt hàng. */
    public static final Duration HAN_THANH_TOAN = Duration.ofMinutes(30);
    private static final DateTimeFormatter NGAY_VNPAY = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String THANH_CONG = "00";

    private final ThanhToanRepository thanhToanRepository;
    private final VnPayProperties vnPay;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record KetQua(boolean hopLe, boolean thanhCong, String maDonHang, String trangThaiThanhToan,
                         String thongBao) {
    }

    /** Mỗi lần gọi tạo một giao dịch mới (mã tham chiếu = mã đơn + số lần thử 2 chữ số). */
    @Transactional
    public String taoUrl(String maDonHang, String ipKhach) {
        if (!vnPay.daCauHinh()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình VNPay");
        }
        ThanhToanRepository.Don don = thanhToanRepository.khoaDon(maDonHang)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy đơn hàng"));
        if (!"vnpay".equals(don.phuongThucThanhToan())) {
            throw ApiException.xungDot("Đơn hàng không thanh toán qua VNPay");
        }
        if ("da_thanh_toan".equals(don.trangThaiThanhToan())) {
            throw ApiException.xungDot("Đơn hàng đã được thanh toán");
        }
        if (!"cho_xac_nhan".equals(don.trangThai())) {
            throw ApiException.xungDot("Đơn hàng không còn chờ thanh toán");
        }
        OffsetDateTime bayGio = OffsetDateTime.now(clock).atZoneSameInstant(ThoiGian.VIET_NAM).toOffsetDateTime();
        OffsetDateTime hetHan = don.createdAt().plus(HAN_THANH_TOAN).atZoneSameInstant(ThoiGian.VIET_NAM).toOffsetDateTime();
        if (!bayGio.isBefore(hetHan)) {
            throw ApiException.xungDot("Đơn hàng đã quá hạn thanh toán");
        }
        int lan = thanhToanRepository.soLanThu(don.id()) + 1;
        if (lan > 99) {
            throw ApiException.xungDot("Đơn hàng đã thử thanh toán quá nhiều lần");
        }
        String maThamChieu = don.maDonHang() + "%02d".formatted(lan);
        thanhToanRepository.themGiaoDich(don.id(), don.tongTien(), maThamChieu);

        Map<String, String> ts = new TreeMap<>();
        ts.put("vnp_Version", "2.1.0");
        ts.put("vnp_Command", "pay");
        ts.put("vnp_TmnCode", vnPay.tmnCode());
        ts.put("vnp_Amount", String.valueOf(don.tongTien() * 100));
        ts.put("vnp_CurrCode", "VND");
        ts.put("vnp_TxnRef", maThamChieu);
        ts.put("vnp_OrderInfo", "Thanh toan don hang " + don.maDonHang());
        ts.put("vnp_OrderType", "other");
        ts.put("vnp_Locale", "vn");
        ts.put("vnp_ReturnUrl", vnPay.returnUrl());
        ts.put("vnp_IpAddr", ipKhach);
        ts.put("vnp_CreateDate", bayGio.format(NGAY_VNPAY));
        ts.put("vnp_ExpireDate", hetHan.format(NGAY_VNPAY));
        return vnPay.payUrl() + "?" + ChuKyVnPay.query(ts) + "&vnp_SecureHash=" + ChuKyVnPay.ky(ts, vnPay.hashSecret());
    }

    /** IPN theo thứ tự VNPay khuyến nghị: chữ ký → giao dịch → số tiền → còn chờ → cập nhật. */
    @Transactional
    public Map<String, String> xuLyIpn(Map<String, String> ts) {
        if (!ChuKyVnPay.hopLe(ts, vnPay.hashSecret())) {
            return phanHoi("97", "Invalid signature");
        }
        ThanhToanRepository.GiaoDich gd = thanhToanRepository.khoaGiaoDich(ts.get("vnp_TxnRef")).orElse(null);
        if (gd == null) {
            return phanHoi("01", "Order not found");
        }
        if (!String.valueOf(gd.soTien() * 100).equals(ts.get("vnp_Amount"))) {
            return phanHoi("04", "Invalid amount");
        }
        if (!"cho_xu_ly".equals(gd.trangThai())) {
            return phanHoi("02", "Order already confirmed");
        }
        boolean thanhCong = THANH_CONG.equals(ts.get("vnp_ResponseCode"))
                && THANH_CONG.equals(ts.get("vnp_TransactionStatus"));
        thanhToanRepository.ketThucGiaoDich(gd.id(), thanhCong, thanhCong ? ts.get("vnp_TransactionNo") : null,
                jsonMapper.writeValueAsString(new TreeMap<>(ts)), OffsetDateTime.now(clock));
        if (thanhCong) {
            thanhToanRepository.danhDauDaThanhToan(gd.donHangId());
        }
        return phanHoi("00", "Confirm Success");
    }

    /** Trang kết quả (Return URL): chỉ đọc, trạng thái trong CSDL có thể chưa đổi nếu IPN chưa tới. */
    @Transactional(readOnly = true)
    public KetQua ketQua(Map<String, String> ts) {
        if (!ChuKyVnPay.hopLe(ts, vnPay.hashSecret())) {
            return new KetQua(false, false, null, null, "Dữ liệu trả về không hợp lệ");
        }
        boolean thanhCong = THANH_CONG.equals(ts.get("vnp_ResponseCode"))
                && THANH_CONG.equals(ts.get("vnp_TransactionStatus"));
        ThanhToanRepository.GiaoDich gd = thanhToanRepository.giaoDich(ts.get("vnp_TxnRef")).orElse(null);
        return new KetQua(true, thanhCong, gd == null ? null : gd.maDonHang(),
                gd == null ? null : gd.trangThaiThanhToan(),
                thanhCong ? "Thanh toán thành công" : "Thanh toán không thành công");
    }

    static Map<String, String> phanHoi(String ma, String thongDiep) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("RspCode", ma);
        m.put("Message", thongDiep);
        return m;
    }
}

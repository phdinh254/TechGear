package com.techgear.donhang;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.PageResponse;
import com.techgear.chung.ThoiGian;
import com.techgear.chung.loi.ApiException;
import com.techgear.sanpham.HienThi;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Quản trị đơn hàng: tìm, lọc, chuyển trạng thái theo sơ đồ, cập nhật thanh toán, ghi chú nội bộ. */
@Service
@RequiredArgsConstructor
public class AdminDonHangService {

    public enum TrangThaiThanhToan { chua_thanh_toan, da_thanh_toan, da_hoan_tien }

    private final JdbcClient jdbc;
    private final DonHangRepository donHangRepository;
    private final DonHangService donHangService;

    public record DoiTrangThaiRequest(
            @NotNull(message = "Vui lòng chọn trạng thái") TrangThaiDonHang trangThai,
            @Size(max = 500, message = "Ghi chú tối đa 500 ký tự") String ghiChu) {
    }

    public record ThanhToanRequest(@NotNull(message = "Vui lòng chọn trạng thái thanh toán") TrangThaiThanhToan trangThaiThanhToan) {
    }

    public record GhiChuRequest(@Size(max = 500, message = "Ghi chú tối đa 500 ký tự") String ghiChuQuanTri) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DongDanhSach(String maDonHang, String tenNguoiNhan, String email, String soDienThoai, long tongTien,
                               String trangThai, String trangThaiThanhToan, String phuongThucThanhToan,
                               long soLuongSanPham, boolean khachVangLai, OffsetDateTime ngayDat) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GiaoDich(String maThamChieu, long soTien, String trangThai, String maGiaoDich,
                           OffsetDateTime thanhToanLuc, OffsetDateTime ngayTao) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChiTietQT(DonHangResponse.ChiTiet don, String ghiChuQuanTri, Long nguoiDungId,
                            List<String> trangThaiTiepTheo, List<GiaoDich> thanhToans) {
    }

    @Transactional(readOnly = true)
    public PageResponse<DongDanhSach> danhSach(String tuKhoa, TrangThaiDonHang trangThai, TrangThaiThanhToan thanhToan,
                                               DatHangRequest.PhuongThucThanhToan phuongThuc, LocalDate tuNgay,
                                               LocalDate denNgay, int trang, int kichThuoc) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw ApiException.yeuCauKhongHopLe("Từ ngày phải trước hoặc bằng đến ngày");
        }
        StringBuilder where = new StringBuilder(" WHERE TRUE");
        Map<String, Object> ts = new HashMap<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            where.append(" ").append("""
                     AND (dh.ma_don_hang = upper(:tuKhoaGoc) OR dh.so_dien_thoai = :tuKhoaGoc
                          OR lower(dh.email) = lower(:tuKhoaGoc)
                          OR lower(unaccent(dh.ten_nguoi_nhan)) LIKE '%' || lower(unaccent(:tuKhoa)) || '%')""");
            ts.put("tuKhoaGoc", tuKhoa.trim());
            ts.put("tuKhoa", HienThi.thoatLike(tuKhoa.trim()));
        }
        if (trangThai != null) {
            where.append(" AND dh.trang_thai = :tt");
            ts.put("tt", trangThai.name());
        }
        if (thanhToan != null) {
            where.append(" AND dh.trang_thai_thanh_toan = :ttt");
            ts.put("ttt", thanhToan.name());
        }
        if (phuongThuc != null) {
            where.append(" AND dh.phuong_thuc_thanh_toan = :pt");
            ts.put("pt", phuongThuc.name());
        }
        if (tuNgay != null) {
            where.append(" AND dh.created_at >= :tu");
            ts.put("tu", tuNgay.atStartOfDay(ThoiGian.VIET_NAM).toOffsetDateTime());
        }
        if (denNgay != null) {
            where.append(" AND dh.created_at < :den");
            ts.put("den", denNgay.plusDays(1).atStartOfDay(ThoiGian.VIET_NAM).toOffsetDateTime());
        }
        long tong = jdbc.sql("SELECT count(*) FROM don_hang dh" + where).params(ts).query(Long.class).single();
        ts.put("gioiHan", kichThuoc);
        ts.put("boQua", (long) trang * kichThuoc);
        List<DongDanhSach> noiDung = tong == 0 ? List.of() : jdbc.sql("""
                        SELECT dh.ma_don_hang, dh.ten_nguoi_nhan, dh.email, dh.so_dien_thoai, dh.tong_tien, dh.trang_thai,
                               dh.trang_thai_thanh_toan, dh.phuong_thuc_thanh_toan,
                               (SELECT sum(c.so_luong) FROM chi_tiet_don_hang c WHERE c.don_hang_id = dh.id) AS so_luong_san_pham,
                               dh.nguoi_dung_id IS NULL AS khach_vang_lai, dh.created_at AS ngay_dat
                        FROM don_hang dh""" + where + """

                        ORDER BY dh.created_at DESC, dh.id DESC LIMIT :gioiHan OFFSET :boQua
                        """)
                .params(ts).query(DongDanhSach.class).list();
        return new PageResponse<>(noiDung, trang, kichThuoc, tong, (int) ((tong + kichThuoc - 1) / kichThuoc));
    }

    @Transactional(readOnly = true)
    public ChiTietQT chiTiet(String maDonHang) {
        Long id = jdbc.sql("SELECT id FROM don_hang WHERE ma_don_hang = upper(:ma)").param("ma", maDonHang)
                .query(Long.class).optional().orElseThrow(AdminDonHangService::khongTimThay);
        return chiTiet(id);
    }

    /**
     * Chuyển trạng thái theo {@link TrangThaiDonHang#coTheChuyenSang}; hủy thì hoàn kho, trả lượt mã;
     * COD hoàn thành thì thành đã thanh toán; đơn VNPay chưa trả không xác nhận được.
     */
    @Transactional
    public ChiTietQT doiTrangThai(String maDonHang, DoiTrangThaiRequest req, Long quanTriId) {
        DonHangRepository.DonKhoa don = donHangRepository.khoaDon(maDonHang).orElseThrow(AdminDonHangService::khongTimThay);
        TrangThaiDonHang cu = TrangThaiDonHang.valueOf(don.trangThai());
        TrangThaiDonHang moi = req.trangThai();
        if (!cu.coTheChuyenSang(moi)) {
            throw ApiException.xungDot("Không chuyển được đơn từ \"" + cu + "\" sang \"" + moi + "\"");
        }
        Map<String, Object> thanhToan = jdbc.sql(
                        "SELECT phuong_thuc_thanh_toan AS pt, trang_thai_thanh_toan AS ttt FROM don_hang WHERE id = :id")
                .param("id", don.id()).query().singleRow();
        boolean vnpayChuaTra = "vnpay".equals(thanhToan.get("pt")) && "chua_thanh_toan".equals(thanhToan.get("ttt"));
        if (moi == TrangThaiDonHang.da_xac_nhan && vnpayChuaTra) {
            throw ApiException.xungDot("Đơn VNPay chưa thanh toán, chưa xác nhận được");
        }
        String ghiChu = req.ghiChu() == null || req.ghiChu().isBlank() ? null : req.ghiChu().trim();
        if (moi == TrangThaiDonHang.da_huy) {
            donHangService.huy(don, quanTriId, ghiChu == null ? "Quản trị hủy đơn" : ghiChu);
        } else {
            donHangRepository.doiTrangThai(don.id(), moi);
            donHangRepository.ghiLichSuDon(don.id(), moi, ghiChu, quanTriId);
            if (moi == TrangThaiDonHang.hoan_thanh && "cod".equals(thanhToan.get("pt"))) {
                datTrangThaiThanhToan(don.id(), TrangThaiThanhToan.da_thanh_toan);
            }
        }
        return chiTiet(don.id());
    }

    /** Chỉ: COD chưa trả → đã trả (đơn chưa hủy); đã trả → đã hoàn tiền (đơn đã hủy). */
    @Transactional
    public ChiTietQT doiThanhToan(String maDonHang, ThanhToanRequest req) {
        DonHangRepository.DonKhoa don = donHangRepository.khoaDon(maDonHang).orElseThrow(AdminDonHangService::khongTimThay);
        Map<String, Object> hienTai = jdbc.sql(
                        "SELECT phuong_thuc_thanh_toan AS pt, trang_thai_thanh_toan AS ttt FROM don_hang WHERE id = :id")
                .param("id", don.id()).query().singleRow();
        boolean daHuy = TrangThaiDonHang.da_huy.name().equals(don.trangThai());
        boolean hopLe = switch (req.trangThaiThanhToan()) {
            case da_thanh_toan -> "cod".equals(hienTai.get("pt")) && "chua_thanh_toan".equals(hienTai.get("ttt")) && !daHuy;
            case da_hoan_tien -> "da_thanh_toan".equals(hienTai.get("ttt")) && daHuy;
            case chua_thanh_toan -> false;
        };
        if (!hopLe) {
            throw ApiException.xungDot("Không cập nhật được trạng thái thanh toán");
        }
        datTrangThaiThanhToan(don.id(), req.trangThaiThanhToan());
        return chiTiet(don.id());
    }

    @Transactional
    public ChiTietQT ghiChu(String maDonHang, GhiChuRequest req) {
        DonHangRepository.DonKhoa don = donHangRepository.khoaDon(maDonHang).orElseThrow(AdminDonHangService::khongTimThay);
        jdbc.sql("UPDATE don_hang SET ghi_chu_quan_tri = :ghiChu, updated_at = now() WHERE id = :id")
                .param("ghiChu", req.ghiChuQuanTri() == null || req.ghiChuQuanTri().isBlank() ? null : req.ghiChuQuanTri().trim())
                .param("id", don.id()).update();
        return chiTiet(don.id());
    }

    private ChiTietQT chiTiet(Long id) {
        DonHangResponse.ChiTiet don = donHangRepository.chiTiet(id, false, true);
        Map<String, Object> them = jdbc.sql("SELECT ghi_chu_quan_tri, nguoi_dung_id FROM don_hang WHERE id = :id")
                .param("id", id).query().singleRow();
        List<GiaoDich> thanhToans = jdbc.sql("""
                        SELECT ma_tham_chieu, so_tien, trang_thai, ma_giao_dich, thanh_toan_luc, created_at AS ngay_tao
                        FROM thanh_toan WHERE don_hang_id = :id ORDER BY id
                        """)
                .param("id", id).query(GiaoDich.class).list();
        TrangThaiDonHang tt = TrangThaiDonHang.valueOf(don.trangThai());
        List<String> tiepTheo = Arrays.stream(TrangThaiDonHang.values()).filter(tt::coTheChuyenSang).map(Enum::name).toList();
        Number nd = (Number) them.get("nguoi_dung_id");
        return new ChiTietQT(don, (String) them.get("ghi_chu_quan_tri"), nd == null ? null : nd.longValue(), tiepTheo,
                thanhToans);
    }

    private void datTrangThaiThanhToan(Long donHangId, TrangThaiThanhToan trangThai) {
        jdbc.sql("UPDATE don_hang SET trang_thai_thanh_toan = :ttt, updated_at = now() WHERE id = :id")
                .param("ttt", trangThai.name()).param("id", donHangId).update();
    }

    private static ApiException khongTimThay() {
        return ApiException.khongTimThay("Không tìm thấy đơn hàng");
    }
}

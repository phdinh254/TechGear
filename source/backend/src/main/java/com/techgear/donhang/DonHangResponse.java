package com.techgear.donhang;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/** Các kiểu dữ liệu trả về của module đơn hàng (xem docs/thiet-ke/dac-ta-api.md). */
public final class DonHangResponse {

    private DonHangResponse() {
    }

    /** Một dòng hàng: lúc tính thử là giá hiện tại, trong đơn là ảnh chụp lúc đặt. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Dong(Long bienTheId, Long sanPhamId, String sanPhamSlug, String tenSanPham, String sku,
                       String phanLoai, String hinhAnh, long donGia, int soLuong, long thanhTien) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TinhThu(List<Dong> dongs, String maGiamGia, long tamTinh, long tienGiamGia, long phiVanChuyen,
                          long tongTien) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TomTat(String maDonHang, String trangThai, String trangThaiThanhToan, String phuongThucThanhToan,
                         long tongTien, long soLuongSanPham, String tenSanPhamDau, String hinhAnhDau,
                         OffsetDateTime ngayDat) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LichSu(String trangThai, String ghiChu, OffsetDateTime thoiGian) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChiTiet(String maDonHang, String trangThai, String trangThaiThanhToan, String phuongThucThanhToan,
                          String tenNguoiNhan, String email, String soDienThoai, String diaChiGiaoHang, String ghiChu,
                          String maGiamGia, long tamTinh, long tienGiamGia, long phiVanChuyen, long tongTien,
                          OffsetDateTime ngayDat, boolean coTheHuy, List<Dong> dongs, List<LichSu> lichSu) {
    }
}

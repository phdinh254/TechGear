package com.techgear.dto;

import com.techgear.entity.ChiTietDonHang;
import com.techgear.entity.DonHang;
import com.techgear.entity.LichSuDonHang;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DonHangResponse(
        Long id,
        String maDonHang,
        String tenNguoiNhan,
        String email,
        String soDienThoai,
        String diaChiGiaoHang,
        String ghiChu,
        BigDecimal tamTinh,
        BigDecimal tienGiamGia,
        BigDecimal phiVanChuyen,
        BigDecimal tongTien,
        String phuongThucThanhToan,
        String trangThaiThanhToan,
        String trangThai,
        LocalDateTime ngayDat,
        List<ChiTiet> chiTiets,
        List<LichSu> lichSus
) {
    public static DonHangResponse tu(DonHang donHang, boolean dayDu) {
        List<ChiTiet> chiTiets = dayDu ? donHang.getChiTiets().stream().map(ChiTiet::tu).toList() : List.of();
        List<LichSu> lichSus = dayDu ? donHang.getLichSus().stream().map(LichSu::tu).toList() : List.of();
        return new DonHangResponse(
                donHang.getId(), donHang.getMaDonHang(), donHang.getTenNguoiNhan(), donHang.getEmail(),
                donHang.getSoDienThoai(), donHang.getDiaChiGiaoHang(), donHang.getGhiChu(), donHang.getTamTinh(),
                donHang.getTienGiamGia(), donHang.getPhiVanChuyen(), donHang.getTongTien(),
                donHang.getPhuongThucThanhToan().name(), donHang.getTrangThaiThanhToan().name(),
                donHang.getTrangThai().name(), donHang.getCreatedAt(), chiTiets, lichSus
        );
    }

    public record ChiTiet(
            Long id, Long sanPhamId, String sanPhamSlug, String tenSanPham, String sku,
            String phanLoai, String hinhAnh, BigDecimal donGia, Integer soLuong, BigDecimal thanhTien
    ) {
        static ChiTiet tu(ChiTietDonHang item) {
            return new ChiTiet(item.getId(), item.getSanPham() == null ? null : item.getSanPham().getId(),
                    item.getSanPham() == null ? null : item.getSanPham().getSlug(), item.getTenSanPham(),
                    item.getSku(), item.getPhanLoai(), item.getHinhAnh(), item.getDonGia(),
                    item.getSoLuong(), item.getThanhTien());
        }
    }

    public record LichSu(String trangThai, String ghiChu, LocalDateTime thoiGian) {
        static LichSu tu(LichSuDonHang item) {
            return new LichSu(item.getTrangThai().name(), item.getGhiChu(), item.getCreatedAt());
        }
    }
}

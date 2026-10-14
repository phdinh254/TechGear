package com.techgear.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Dòng đơn hàng: lưu lại tên, SKU, giá tại thời điểm mua. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chi_tiet_don_hang")
public class ChiTietDonHang extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "don_hang_id", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_id")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bien_the_id")
    private BienTheSanPham bienThe;

    @Column(name = "ten_san_pham", nullable = false)
    private String tenSanPham;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "phan_loai", length = 100)
    private String phanLoai;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "don_gia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    /** Phải bằng donGia x soLuong (CSDL có ràng buộc CHECK). */
    @Column(name = "thanh_tien", nullable = false, precision = 12, scale = 0)
    private BigDecimal thanhTien;
}

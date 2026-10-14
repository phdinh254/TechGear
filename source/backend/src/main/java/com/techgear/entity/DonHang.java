package com.techgear.entity;

import com.techgear.entity.enums.PhuongThucThanhToan;
import com.techgear.entity.enums.TrangThaiDonHang;
import com.techgear.entity.enums.TrangThaiThanhToan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "don_hang")
@SQLDelete(sql = "UPDATE don_hang SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DonHang extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_don_hang", nullable = false, unique = true, length = 30)
    private String maDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_giam_gia_id")
    private MaGiamGia maGiamGia;

    @Column(name = "ten_nguoi_nhan", nullable = false, length = 100)
    private String tenNguoiNhan;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String soDienThoai;

    @Column(name = "dia_chi_giao_hang", nullable = false, length = 500)
    private String diaChiGiaoHang;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "ghi_chu_quan_tri", length = 500)
    private String ghiChuQuanTri;

    @Column(name = "tam_tinh", nullable = false, precision = 12, scale = 0)
    private BigDecimal tamTinh;

    @Column(name = "tien_giam_gia", nullable = false, precision = 12, scale = 0)
    private BigDecimal tienGiamGia = BigDecimal.ZERO;

    @Column(name = "phi_van_chuyen", nullable = false, precision = 12, scale = 0)
    private BigDecimal phiVanChuyen = BigDecimal.ZERO;

    /** Phải bằng tamTinh - tienGiamGia + phiVanChuyen (CSDL có ràng buộc CHECK). */
    @Column(name = "tong_tien", nullable = false, precision = 12, scale = 0)
    private BigDecimal tongTien;

    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc_thanh_toan", nullable = false)
    private PhuongThucThanhToan phuongThucThanhToan = PhuongThucThanhToan.cod;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_thanh_toan", nullable = false)
    private TrangThaiThanhToan trangThaiThanhToan = TrangThaiThanhToan.chua_thanh_toan;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiDonHang trangThai = TrangThaiDonHang.cho_xac_nhan;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ChiTietDonHang> chiTiets = new ArrayList<>();

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<LichSuDonHang> lichSus = new ArrayList<>();
}

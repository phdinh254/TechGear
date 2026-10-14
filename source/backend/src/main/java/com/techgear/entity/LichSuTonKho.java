package com.techgear.entity;

import com.techgear.entity.enums.LoaiBienDongKho;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lich_su_ton_kho")
public class LichSuTonKho extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bien_the_id", nullable = false)
    private BienTheSanPham bienThe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chi_tiet_don_hang_id")
    private ChiTietDonHang chiTietDonHang;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoaiBienDongKho loai;

    /** Dương = tăng, âm = giảm. */
    @Column(name = "so_luong_thay_doi", nullable = false)
    private Integer soLuongThayDoi;

    @Column(name = "so_luong_sau", nullable = false)
    private Integer soLuongSau;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_thuc_hien_id")
    private NguoiDung nguoiThucHien;
}

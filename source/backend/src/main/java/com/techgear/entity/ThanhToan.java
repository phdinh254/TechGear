package com.techgear.entity;

import com.techgear.entity.enums.PhuongThucThanhToan;
import com.techgear.entity.enums.TrangThaiGiaoDich;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "thanh_toan")
public class ThanhToan extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "don_hang_id", nullable = false)
    private DonHang donHang;

    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc", nullable = false)
    private PhuongThucThanhToan phuongThuc;

    @Column(name = "so_tien", nullable = false, precision = 12, scale = 0)
    private BigDecimal soTien;

    @Column(name = "ma_giao_dich", unique = true, length = 100)
    private String maGiaoDich;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiGiaoDich trangThai = TrangThaiGiaoDich.cho_xu_ly;

    /** Chuỗi JSON do cổng thanh toán trả về. */
    @Column(name = "du_lieu_phan_hoi", columnDefinition = "JSON")
    private String duLieuPhanHoi;

    @Column(name = "thanh_toan_luc")
    private LocalDateTime thanhToanLuc;
}

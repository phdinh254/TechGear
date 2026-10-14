package com.techgear.entity;

import com.techgear.entity.enums.LoaiGiam;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ma_giam_gia")
@SQLDelete(sql = "UPDATE ma_giam_gia SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MaGiamGia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "mo_ta")
    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_giam", nullable = false)
    private LoaiGiam loaiGiam;

    @Column(name = "gia_tri_giam", nullable = false, precision = 12, scale = 0)
    private BigDecimal giaTriGiam;

    @Column(name = "don_toi_thieu", nullable = false, precision = 12, scale = 0)
    private BigDecimal donToiThieu = BigDecimal.ZERO;

    @Column(name = "giam_toi_da", precision = 12, scale = 0)
    private BigDecimal giamToiDa;

    @Column(name = "tong_luot_dung")
    private Integer tongLuotDung;

    @Column(name = "luot_dung_moi_nguoi")
    private Integer luotDungMoiNguoi;

    @Column(name = "da_dung", nullable = false)
    private Integer daDung = 0;

    @Column(name = "bat_dau_luc", nullable = false)
    private LocalDateTime batDauLuc;

    @Column(name = "ket_thuc_luc", nullable = false)
    private LocalDateTime ketThucLuc;

    @Column(name = "kich_hoat", nullable = false)
    private Boolean kichHoat = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

package com.techgear.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "hinh_anh_san_pham")
public class HinhAnhSanPham extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    /** Ảnh gắn theo màu; null = ảnh chung. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gia_tri_tuy_chon_id")
    private GiaTriTuyChon giaTriTuyChon;

    @Column(name = "duong_dan", nullable = false)
    private String duongDan;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu = 0;

    @Column(name = "la_anh_chinh", nullable = false)
    private Boolean laAnhChinh = false;
}

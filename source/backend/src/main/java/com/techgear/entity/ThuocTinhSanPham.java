package com.techgear.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Thông số kỹ thuật của sản phẩm (Công suất, Cổng kết nối, ...). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "thuoc_tinh_san_pham")
public class ThuocTinhSanPham extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @Column(nullable = false, length = 100)
    private String ten;

    @Column(name = "gia_tri", nullable = false)
    private String giaTri;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu = 0;
}

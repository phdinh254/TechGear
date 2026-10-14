package com.techgear.entity;

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
@Table(name = "san_pham")
@SQLDelete(sql = "UPDATE san_pham SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SanPham extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "danh_muc_id", nullable = false)
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thuong_hieu_id")
    private ThuongHieu thuongHieu;

    @Column(nullable = false)
    private String ten;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "mo_ta_ngan", length = 500)
    private String moTaNgan;

    @Column(name = "mo_ta", columnDefinition = "LONGTEXT")
    private String moTa;

    @Column(name = "gia_ban", nullable = false, precision = 12, scale = 0)
    private BigDecimal giaBan;

    @Column(name = "gia_goc", precision = 12, scale = 0)
    private BigDecimal giaGoc;

    @Column(name = "bao_hanh_thang", nullable = false)
    private Integer baoHanhThang = 0;

    @Column(name = "noi_bat", nullable = false)
    private Boolean noiBat = false;

    @Column(name = "hien_thi", nullable = false)
    private Boolean hienThi = true;

    @Column(name = "luot_xem", nullable = false)
    private Integer luotXem = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("thuTu ASC")
    private List<HinhAnhSanPham> hinhAnhs = new ArrayList<>();

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("thuTu ASC")
    private List<ThuocTinhSanPham> thuocTinhs = new ArrayList<>();

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("thuTu ASC")
    private List<TuyChon> tuyChons = new ArrayList<>();

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<BienTheSanPham> bienThes = new ArrayList<>();
}

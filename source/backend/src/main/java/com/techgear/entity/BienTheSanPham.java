package com.techgear.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/** Biến thể sản phẩm: đơn vị quản lý SKU, giá và tồn kho. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bien_the_san_pham")
@SQLDelete(sql = "UPDATE bien_the_san_pham SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class BienTheSanPham extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "gia_ban", precision = 12, scale = 0)
    private BigDecimal giaBan;

    @Column(name = "gia_goc", precision = 12, scale = 0)
    private BigDecimal giaGoc;

    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon = 0;

    @Column(name = "nguong_canh_bao", nullable = false)
    private Integer nguongCanhBao = 5;

    @Column(name = "hien_thi", nullable = false)
    private Boolean hienThi = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Các giá trị tùy chọn tạo nên biến thể, VD: Đen + 20000mAh. */
    @ManyToMany
    @JoinTable(name = "bien_the_gia_tri",
            joinColumns = @JoinColumn(name = "bien_the_id"),
            inverseJoinColumns = @JoinColumn(name = "gia_tri_tuy_chon_id"))
    private Set<GiaTriTuyChon> giaTris = new LinkedHashSet<>();

    /** Giá bán thực tế: giá riêng của biến thể, nếu không có thì lấy giá sản phẩm. */
    public BigDecimal layGiaBan() {
        return giaBan != null ? giaBan : sanPham.getGiaBan();
    }

    /** Giá gốc thực tế: giá gốc riêng của biến thể, nếu không có thì lấy giá gốc sản phẩm. */
    public BigDecimal layGiaGoc() {
        return giaGoc != null ? giaGoc : sanPham.getGiaGoc();
    }
}

package com.techgear.entity;

import com.techgear.entity.enums.LoaiTuyChon;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Tùy chọn của sản phẩm: Màu sắc, Dung lượng, Phiên bản... */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tuy_chon")
public class TuyChon extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @Column(nullable = false, length = 50)
    private String ten;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoaiTuyChon loai = LoaiTuyChon.khac;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu = 0;

    @OneToMany(mappedBy = "tuyChon", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("thuTu ASC")
    private List<GiaTriTuyChon> giaTris = new ArrayList<>();
}

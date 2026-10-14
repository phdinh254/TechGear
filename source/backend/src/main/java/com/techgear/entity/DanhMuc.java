package com.techgear.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "danh_muc")
@SQLDelete(sql = "UPDATE danh_muc SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DanhMuc extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "danh_muc_cha_id")
    private DanhMuc danhMucCha;

    @OneToMany(mappedBy = "danhMucCha")
    @OrderBy("thuTu ASC")
    private List<DanhMuc> danhMucCons = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String ten;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu = 0;

    @Column(name = "hien_thi", nullable = false)
    private Boolean hienThi = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

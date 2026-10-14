package com.techgear.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "banner")
public class Banner extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(name = "hinh_anh", nullable = false)
    private String hinhAnh;

    @Column(name = "lien_ket")
    private String lienKet;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu = 0;

    @Column(name = "hien_thi", nullable = false)
    private Boolean hienThi = true;

    @Column(name = "bat_dau_luc")
    private LocalDateTime batDauLuc;

    @Column(name = "ket_thuc_luc")
    private LocalDateTime ketThucLuc;
}

package com.techgear.entity;

import com.techgear.entity.enums.KieuCaiDat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cai_dat")
public class CaiDat extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String khoa;

    @Column(name = "gia_tri", columnDefinition = "TEXT")
    private String giaTri;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KieuCaiDat kieu = KieuCaiDat.chuoi;

    @Column(name = "mo_ta")
    private String moTa;
}

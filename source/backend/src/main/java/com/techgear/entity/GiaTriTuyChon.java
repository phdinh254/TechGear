package com.techgear.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "gia_tri_tuy_chon")
public class GiaTriTuyChon extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tuy_chon_id", nullable = false)
    private TuyChon tuyChon;

    @Column(name = "gia_tri", nullable = false, length = 50)
    private String giaTri;

    @Column(name = "ma_mau", length = 7)
    private String maMau;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu = 0;
}

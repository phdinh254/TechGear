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
@Table(name = "ma_dat_lai_mat_khau")
public class MaDatLaiMatKhau extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_dung_id", nullable = false)
    private NguoiDung nguoiDung;

    @Column(nullable = false, unique = true, length = 100)
    private String ma;

    @Column(name = "het_han_luc", nullable = false)
    private LocalDateTime hetHanLuc;

    @Column(name = "da_su_dung", nullable = false)
    private Boolean daSuDung = false;
}

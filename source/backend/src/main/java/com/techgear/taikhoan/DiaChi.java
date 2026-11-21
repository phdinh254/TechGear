package com.techgear.taikhoan;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "dia_chi")
@Getter
@Setter
public class DiaChi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long nguoiDungId;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String tinhThanh;
    private String phuongXa;
    private String diaChiChiTiet;
    private boolean laMacDinh;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}

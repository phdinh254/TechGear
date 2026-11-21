package com.techgear.taikhoan;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/** Mã đặt lại mật khẩu: chỉ lưu SHA-256 của mã gửi qua email. */
@Entity
@Table(name = "ma_dat_lai_mat_khau")
@Getter
@Setter
public class MaDatLaiMatKhau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long nguoiDungId;

    @JdbcTypeCode(SqlTypes.CHAR)
    private String maBam;

    private OffsetDateTime hetHanLuc;

    private boolean daSuDung;
}

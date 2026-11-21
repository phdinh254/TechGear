package com.techgear.taikhoan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
public class NguoiDung {

    /** Giá trị viết thường khớp CHECK trong CSDL và JSON trả frontend. */
    public enum VaiTro { quan_tri, khach_hang }

    public enum TrangThai { hoat_dong, bi_khoa }

    public enum GioiTinh { nam, nu, khac }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hoTen;
    private String email;
    private String matKhau;
    private String soDienThoai;

    @Enumerated(EnumType.STRING)
    private GioiTinh gioiTinh;

    private LocalDate ngaySinh;
    private String anhDaiDien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VaiTro vaiTro = VaiTro.khach_hang;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrangThai trangThai = TrangThai.hoat_dong;

    /** Token phát hành trước mốc này bị từ chối. */
    private OffsetDateTime doiMatKhauLuc;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}

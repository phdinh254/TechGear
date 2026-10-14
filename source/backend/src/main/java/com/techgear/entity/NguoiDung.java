package com.techgear.entity;

import com.techgear.entity.enums.GioiTinh;
import com.techgear.entity.enums.TrangThaiTaiKhoan;
import com.techgear.entity.enums.VaiTro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "nguoi_dung")
@SQLDelete(sql = "UPDATE nguoi_dung SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class NguoiDung extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Enumerated(EnumType.STRING)
    @Column(name = "gioi_tinh")
    private GioiTinh gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "anh_dai_dien")
    private String anhDaiDien;

    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false)
    private VaiTro vaiTro = VaiTro.khach_hang;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiTaiKhoan trangThai = TrangThaiTaiKhoan.hoat_dong;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

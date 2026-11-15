package com.techgear.taikhoan;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    /** {@code email} đã chuẩn hóa chữ thường (cột luôn lưu chữ thường). */
    Optional<NguoiDung> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Khóa dòng người dùng (SELECT ... FOR UPDATE) để tuần tự hóa các thao tác trên sổ địa chỉ, giỏ hàng, yêu thích của họ. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NguoiDung n where n.id = :id")
    Optional<NguoiDung> khoaTheoId(Long id);
}

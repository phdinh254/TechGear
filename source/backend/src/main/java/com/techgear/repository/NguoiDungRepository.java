package com.techgear.repository;

import com.techgear.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Email kể cả tài khoản đã xóa mềm (cột email UNIQUE trong CSDL). */
    @Query(value = "SELECT COUNT(*) FROM nguoi_dung WHERE email = :email", nativeQuery = true)
    long demEmailKeCaDaXoa(@Param("email") String email);
}

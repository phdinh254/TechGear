package com.techgear.repository;

import com.techgear.entity.MaDatLaiMatKhau;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MaDatLaiMatKhauRepository extends JpaRepository<MaDatLaiMatKhau, Long> {

    Optional<MaDatLaiMatKhau> findByMa(String ma);
}

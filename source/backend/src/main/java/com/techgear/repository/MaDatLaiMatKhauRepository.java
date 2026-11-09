package com.techgear.repository;

import com.techgear.entity.MaDatLaiMatKhau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaDatLaiMatKhauRepository extends JpaRepository<MaDatLaiMatKhau, Long> {

    /** Tìm theo mã đã băm SHA-256 (CSDL không lưu mã gốc). */
    Optional<MaDatLaiMatKhau> findByMa(String maDaBam);

    Optional<MaDatLaiMatKhau> findFirstByNguoiDungIdOrderByCreatedAtDesc(Long nguoiDungId);

    List<MaDatLaiMatKhau> findByNguoiDungIdAndDaSuDungFalse(Long nguoiDungId);
}

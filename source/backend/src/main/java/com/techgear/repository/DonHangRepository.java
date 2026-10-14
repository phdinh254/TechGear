package com.techgear.repository;

import com.techgear.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {

    Optional<DonHang> findByMaDonHang(String maDonHang);

    Page<DonHang> findByNguoiDungIdOrderByCreatedAtDesc(Long nguoiDungId, Pageable pageable);
}

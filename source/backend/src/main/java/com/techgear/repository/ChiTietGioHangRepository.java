package com.techgear.repository;

import com.techgear.entity.ChiTietGioHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, Long> {

    Optional<ChiTietGioHang> findByGioHangIdAndBienTheId(Long gioHangId, Long bienTheId);
}

package com.techgear.repository;

import com.techgear.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long> {

    Optional<ThanhToan> findByMaGiaoDich(String maGiaoDich);
}

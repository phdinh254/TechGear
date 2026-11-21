package com.techgear.repository;

import com.techgear.entity.LichSuTonKho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LichSuTonKhoRepository extends JpaRepository<LichSuTonKho, Long> {

    Page<LichSuTonKho> findByBienTheIdOrderByCreatedAtDescIdDesc(Long bienTheId, Pageable pageable);
}

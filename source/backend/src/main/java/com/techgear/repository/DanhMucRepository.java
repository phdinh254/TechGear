package com.techgear.repository;

import com.techgear.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {

    List<DanhMuc> findByHienThiTrueOrderByThuTuAsc();

    Optional<DanhMuc> findBySlug(String slug);

    boolean existsBySlug(String slug);
}

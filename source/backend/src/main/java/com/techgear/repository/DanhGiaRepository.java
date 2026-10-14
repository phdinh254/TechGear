package com.techgear.repository;

import com.techgear.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {

    List<DanhGia> findBySanPhamIdAndDaDuyetTrueOrderByCreatedAtDesc(Long sanPhamId);
}

package com.techgear.repository;

import com.techgear.entity.YeuThich;
import org.springframework.data.jpa.repository.JpaRepository;
import com.techgear.entity.YeuThichId;

import java.util.List;

public interface YeuThichRepository extends JpaRepository<YeuThich, YeuThichId> {

    List<YeuThich> findByNguoiDungIdOrderByCreatedAtDesc(Long nguoiDungId);
}

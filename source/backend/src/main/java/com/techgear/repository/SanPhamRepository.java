package com.techgear.repository;

import com.techgear.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SanPhamRepository extends JpaRepository<SanPham, Long>, JpaSpecificationExecutor<SanPham> {

    Optional<SanPham> findBySlugAndHienThiTrue(String slug);

    List<SanPham> findTop8ByHienThiTrueAndNoiBatTrueOrderByIdDesc();

    @Modifying
    @Query("UPDATE SanPham s SET s.luotXem = s.luotXem + 1 WHERE s.id = :id")
    int tangLuotXem(@Param("id") Long id);
}

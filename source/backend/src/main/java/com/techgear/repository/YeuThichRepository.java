package com.techgear.repository;

import com.techgear.entity.YeuThich;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.techgear.entity.YeuThichId;

import java.util.List;

public interface YeuThichRepository extends JpaRepository<YeuThich, YeuThichId> {

    List<YeuThich> findByNguoiDungIdOrderByCreatedAtDesc(Long nguoiDungId);

    boolean existsByNguoiDungIdAndSanPhamId(Long nguoiDungId, Long sanPhamId);

    @Modifying
    @Query("DELETE FROM YeuThich y WHERE y.sanPham.id = :spId")
    int xoaTheoSanPham(@Param("spId") Long spId);
}

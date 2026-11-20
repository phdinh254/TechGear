package com.techgear.repository;

import com.techgear.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByHienThiTrueOrderByThuTuAsc();

    /** Banner đang bật và nằm trong khung thời gian hiển thị (mốc null = không giới hạn). */
    @Query("""
            SELECT b FROM Banner b
            WHERE b.hienThi = true
              AND (b.batDauLuc IS NULL OR b.batDauLuc <= :thoiDiem)
              AND (b.ketThucLuc IS NULL OR b.ketThucLuc >= :thoiDiem)
            ORDER BY b.thuTu ASC, b.id ASC
            """)
    List<Banner> findDangHienThi(@Param("thoiDiem") LocalDateTime thoiDiem);
}

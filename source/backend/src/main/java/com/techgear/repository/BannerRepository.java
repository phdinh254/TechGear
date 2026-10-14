package com.techgear.repository;

import com.techgear.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByHienThiTrueOrderByThuTuAsc();
}

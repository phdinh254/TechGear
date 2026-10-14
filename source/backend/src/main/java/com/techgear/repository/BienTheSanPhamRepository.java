package com.techgear.repository;

import com.techgear.entity.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {

    Optional<BienTheSanPham> findBySku(String sku);
}

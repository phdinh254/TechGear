package com.techgear.repository;

import com.techgear.entity.BienTheSanPham;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {

    Optional<BienTheSanPham> findBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BienTheSanPham b WHERE b.id = :id")
    Optional<BienTheSanPham> findByIdForUpdate(@Param("id") Long id);
}

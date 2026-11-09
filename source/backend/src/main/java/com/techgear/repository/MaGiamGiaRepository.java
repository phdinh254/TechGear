package com.techgear.repository;

import com.techgear.entity.MaGiamGia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Long> {

    Optional<MaGiamGia> findByMaIgnoreCase(String ma);

    @Query("""
            SELECT m FROM MaGiamGia m
            WHERE m.kichHoat = true
              AND m.batDauLuc <= :lucNay
              AND m.ketThucLuc >= :lucNay
              AND (m.tongLuotDung IS NULL OR m.daDung < m.tongLuotDung)
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<MaGiamGia> timMaDangApDung(@Param("lucNay") LocalDateTime lucNay, Pageable pageable);
}

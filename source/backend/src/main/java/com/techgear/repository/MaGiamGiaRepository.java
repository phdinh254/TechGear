package com.techgear.repository;

import com.techgear.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Long> {

    Optional<MaGiamGia> findByMaIgnoreCase(String ma);
}

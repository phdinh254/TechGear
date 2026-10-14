package com.techgear.repository;

import com.techgear.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Long> {

    List<ThuongHieu> findByHienThiTrueOrderByTenAsc();

    Optional<ThuongHieu> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}

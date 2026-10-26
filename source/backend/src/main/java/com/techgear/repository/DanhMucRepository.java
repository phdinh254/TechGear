package com.techgear.repository;

import com.techgear.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {

    List<DanhMuc> findByHienThiTrueOrderByThuTuAsc();

    Optional<DanhMuc> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /** Đếm slug kể cả bản ghi đã xóa mềm (cột slug UNIQUE trong CSDL), bỏ qua bản ghi có id = boQuaId. */
    @Query(value = "SELECT COUNT(*) FROM danh_muc WHERE slug = :slug AND id <> :boQuaId", nativeQuery = true)
    long demSlugKeCaDaXoa(@Param("slug") String slug, @Param("boQuaId") long boQuaId);
}

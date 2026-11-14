package com.techgear.repository;

import com.techgear.entity.DanhGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {

    @EntityGraph(attributePaths = "nguoiDung")
    Page<DanhGia> findBySanPhamIdAndDaDuyetTrue(Long sanPhamId, Pageable pageable);

    long countBySanPhamIdAndDaDuyetTrue(Long sanPhamId);

    /** NULL khi sản phẩm chưa có đánh giá đã duyệt. */
    @Query("SELECT AVG(d.soSao) FROM DanhGia d WHERE d.sanPham.id = :sanPhamId AND d.daDuyet = true")
    Double tinhSoSaoTrungBinh(@Param("sanPhamId") Long sanPhamId);
}

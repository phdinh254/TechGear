package com.techgear.repository;

import com.techgear.entity.DanhGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {

    @EntityGraph(attributePaths = "nguoiDung")
    Page<DanhGia> findBySanPhamIdAndDaDuyetTrueOrderByCreatedAtDesc(Long sanPhamId, Pageable pageable);

    @Query("SELECT AVG(d.soSao) FROM DanhGia d WHERE d.sanPham.id = :sanPhamId AND d.daDuyet = true")
    Double tinhDiemTrungBinhDaDuyet(@Param("sanPhamId") Long sanPhamId);

    boolean existsByNguoiDungIdAndSanPhamIdAndDonHangId(Long nguoiDungId, Long sanPhamId, Long donHangId);

    Optional<DanhGia> findByNguoiDungIdAndSanPhamIdAndDonHangId(Long nguoiDungId, Long sanPhamId, Long donHangId);
}

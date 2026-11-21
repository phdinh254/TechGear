package com.techgear.repository;

import com.techgear.entity.ChiTietGioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, Long> {

    Optional<ChiTietGioHang> findByGioHangIdAndBienTheId(Long gioHangId, Long bienTheId);

    @Modifying
    @Query("DELETE FROM ChiTietGioHang c WHERE c.bienThe.id IN :ids")
    int xoaTheoBienThe(@Param("ids") Collection<Long> ids);

    /** Gỡ mọi biến thể chưa xóa của sản phẩm khỏi giỏ hàng: gọi trước khi xóa mềm biến thể. */
    @Modifying
    @Query("DELETE FROM ChiTietGioHang c WHERE c.bienThe.id IN (SELECT b.id FROM BienTheSanPham b WHERE b.sanPham.id = :spId)")
    int xoaTheoSanPham(@Param("spId") Long spId);
}

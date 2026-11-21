package com.techgear.repository;

import com.techgear.entity.BienTheSanPham;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {

    Optional<BienTheSanPham> findBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BienTheSanPham b WHERE b.id = :id")
    Optional<BienTheSanPham> findByIdForUpdate(@Param("id") Long id);

    /** Id sản phẩm đang giữ SKU, kể cả biến thể đã xóa mềm (cột sku UNIQUE trong CSDL). */
    @Query(value = "SELECT san_pham_id FROM bien_the_san_pham WHERE sku = :sku", nativeQuery = true)
    List<Long> timSanPhamIdTheoSkuKeCaDaXoa(@Param("sku") String sku);

    /** Xóa mềm mọi biến thể của sản phẩm và giải phóng SKU ("ABC" -> "ABC#id"), giống SkuUtil.giaiPhong. */
    @Modifying
    @Query(value = "UPDATE bien_the_san_pham SET deleted_at = NOW(), sku = CONCAT(LEFT(sku, 99 - CHAR_LENGTH(id)), '#', id) WHERE san_pham_id = :spId AND deleted_at IS NULL", nativeQuery = true)
    int xoaMemTheoSanPham(@Param("spId") Long spId);

    /**
     * Danh sách tồn kho cho quản trị. tuKhoa: null hoặc "%x%" chữ thường; trangThai: null | con_hang | sap_het | het_hang.
     * Thứ tự cố định (sắp hết, hết hàng, còn hàng) nên truyền Pageable không kèm sort.
     */
    @Query(value = """
            SELECT b FROM BienTheSanPham b JOIN b.sanPham s
            WHERE (:tuKhoa IS NULL OR LOWER(s.ten) LIKE :tuKhoa OR LOWER(b.sku) LIKE :tuKhoa)
              AND (:trangThai IS NULL
                   OR (:trangThai = 'het_hang' AND b.soLuongTon <= 0)
                   OR (:trangThai = 'sap_het'  AND b.soLuongTon > 0 AND b.soLuongTon <= b.nguongCanhBao)
                   OR (:trangThai = 'con_hang' AND b.soLuongTon > b.nguongCanhBao))
            ORDER BY CASE WHEN b.soLuongTon > 0 AND b.soLuongTon <= b.nguongCanhBao THEN 0
                          WHEN b.soLuongTon <= 0 THEN 1 ELSE 2 END, s.ten, b.id
            """,
            countQuery = """
            SELECT COUNT(b) FROM BienTheSanPham b JOIN b.sanPham s
            WHERE (:tuKhoa IS NULL OR LOWER(s.ten) LIKE :tuKhoa OR LOWER(b.sku) LIKE :tuKhoa)
              AND (:trangThai IS NULL
                   OR (:trangThai = 'het_hang' AND b.soLuongTon <= 0)
                   OR (:trangThai = 'sap_het'  AND b.soLuongTon > 0 AND b.soLuongTon <= b.nguongCanhBao)
                   OR (:trangThai = 'con_hang' AND b.soLuongTon > b.nguongCanhBao))
            """)
    Page<BienTheSanPham> timChoQuanTri(@Param("tuKhoa") String tuKhoa, @Param("trangThai") String trangThai,
                                       Pageable pageable);
}

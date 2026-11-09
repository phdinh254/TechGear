package com.techgear.repository;

import com.techgear.entity.DonHang;
import com.techgear.entity.enums.TrangThaiDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {

    Optional<DonHang> findByMaDonHang(String maDonHang);

    Page<DonHang> findByNguoiDungIdOrderByCreatedAtDesc(Long nguoiDungId, Pageable pageable);

    long countByNguoiDungIdAndMaGiamGiaIdAndTrangThaiNot(
            Long nguoiDungId, Long maGiamGiaId, TrangThaiDonHang trangThai);

    Optional<DonHang> findByMaDonHangAndNguoiDungId(String maDonHang, Long nguoiDungId);

    Optional<DonHang> findByIdAndNguoiDungId(Long id, Long nguoiDungId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DonHang d WHERE d.maDonHang = :maDonHang AND d.nguoiDung.id = :nguoiDungId")
    Optional<DonHang> findCuaNguoiDungForUpdate(@Param("maDonHang") String maDonHang,
                                                @Param("nguoiDungId") Long nguoiDungId);
}

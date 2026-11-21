package com.techgear.taikhoan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaChiRepository extends JpaRepository<DiaChi, Long> {

    List<DiaChi> findByNguoiDungIdOrderByLaMacDinhDescIdDesc(Long nguoiDungId);

    long countByNguoiDungId(Long nguoiDungId);

    Optional<DiaChi> findByIdAndNguoiDungId(Long id, Long nguoiDungId);

    Optional<DiaChi> findFirstByNguoiDungIdOrderByIdDesc(Long nguoiDungId);

    Optional<DiaChi> findByNguoiDungIdAndLaMacDinhTrue(Long nguoiDungId);
}

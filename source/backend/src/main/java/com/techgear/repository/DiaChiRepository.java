package com.techgear.repository;

import com.techgear.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaChiRepository extends JpaRepository<DiaChi, Long> {

    /** Địa chỉ mặc định lên đầu, sau đó mới nhất trước. */
    List<DiaChi> findByNguoiDungIdOrderByLaMacDinhDescIdDesc(Long nguoiDungId);

    Optional<DiaChi> findByIdAndNguoiDungId(Long id, Long nguoiDungId);

    long countByNguoiDungId(Long nguoiDungId);
}

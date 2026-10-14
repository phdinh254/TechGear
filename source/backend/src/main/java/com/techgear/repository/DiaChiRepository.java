package com.techgear.repository;

import com.techgear.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiaChiRepository extends JpaRepository<DiaChi, Long> {

    List<DiaChi> findByNguoiDungIdOrderByLaMacDinhDescIdDesc(Long nguoiDungId);
}

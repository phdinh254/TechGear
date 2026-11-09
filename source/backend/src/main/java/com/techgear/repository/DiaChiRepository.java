package com.techgear.repository;

import com.techgear.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DiaChiRepository extends JpaRepository<DiaChi, Long> {

    List<DiaChi> findByNguoiDungIdOrderByLaMacDinhDescIdDesc(Long nguoiDungId);

    Optional<DiaChi> findByIdAndNguoiDungId(Long id, Long nguoiDungId);

    @Modifying
    @Query("UPDATE DiaChi d SET d.laMacDinh = false WHERE d.nguoiDung.id = :nguoiDungId")
    int boMacDinhCuaNguoiDung(@Param("nguoiDungId") Long nguoiDungId);

    @Modifying
    @Query("UPDATE DiaChi d SET d.laMacDinh = false WHERE d.nguoiDung.id = :nguoiDungId AND d.id <> :diaChiId")
    int boMacDinhKhac(@Param("nguoiDungId") Long nguoiDungId, @Param("diaChiId") Long diaChiId);
}

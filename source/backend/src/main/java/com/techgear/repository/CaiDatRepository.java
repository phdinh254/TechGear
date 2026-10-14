package com.techgear.repository;

import com.techgear.entity.CaiDat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CaiDatRepository extends JpaRepository<CaiDat, Long> {

    Optional<CaiDat> findByKhoa(String khoa);
}

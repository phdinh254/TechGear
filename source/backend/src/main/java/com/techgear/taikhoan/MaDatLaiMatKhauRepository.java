package com.techgear.taikhoan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface MaDatLaiMatKhauRepository extends JpaRepository<MaDatLaiMatKhau, Long> {

    Optional<MaDatLaiMatKhau> findByMaBam(String maBam);

    /** Đánh dấu đã dùng nếu còn hiệu lực – trả 0 khi mã đã dùng/hết hạn (hai yêu cầu đồng thời chỉ một bên thắng). */
    @Modifying
    @Query("update MaDatLaiMatKhau m set m.daSuDung = true "
            + "where m.id = :id and m.daSuDung = false and m.hetHanLuc > :bayGio")
    int suDung(Long id, OffsetDateTime bayGio);

    @Modifying
    @Query("update MaDatLaiMatKhau m set m.daSuDung = true where m.nguoiDungId = :nguoiDungId and m.daSuDung = false")
    void huyTatCa(Long nguoiDungId);
}

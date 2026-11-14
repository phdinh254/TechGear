package com.techgear.chung.baomat;

import com.techgear.chung.loi.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GioiHanSoLanTest {

    /** Đồng hồ điều khiển được để thử cửa sổ thời gian mà không phải chờ. */
    static class DongHoGia extends Clock {
        Instant luc = Instant.parse("2026-11-15T00:00:00Z");

        void tien(Duration d) {
            luc = luc.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return luc;
        }
    }

    private final DongHoGia dongHo = new DongHoGia();
    private final GioiHanSoLan gioiHan = new GioiHanSoLan(dongHo);
    private static final Duration CUA_SO = Duration.ofMinutes(15);

    @Test
    void chanKhiDuSoLanTrongCuaSo() {
        for (int i = 0; i < 5; i++) {
            gioiHan.kiemTra("dang-nhap:a@x.vn", 5, CUA_SO);
            gioiHan.ghiNhan("dang-nhap:a@x.vn");
        }
        ApiException e = assertThrows(ApiException.class, () -> gioiHan.kiemTra("dang-nhap:a@x.vn", 5, CUA_SO));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getTrangThai());
    }

    @Test
    void moLaiKhiLanThuCuNhatRaKhoiCuaSo() {
        for (int i = 0; i < 5; i++) {
            gioiHan.ghiNhan("k");
            dongHo.tien(Duration.ofMinutes(1));
        }
        assertThrows(ApiException.class, () -> gioiHan.kiemTra("k", 5, CUA_SO));
        dongHo.tien(Duration.ofMinutes(11)); // lần đầu (phút 0) đã quá 15 phút
        assertDoesNotThrow(() -> gioiHan.kiemTra("k", 5, CUA_SO));
    }

    @Test
    void cacKhoaDocLapVaXoaThiDemLai() {
        for (int i = 0; i < 5; i++) {
            gioiHan.ghiNhan("a");
        }
        assertDoesNotThrow(() -> gioiHan.kiemTra("b", 5, CUA_SO));
        gioiHan.xoa("a");
        assertDoesNotThrow(() -> gioiHan.kiemTra("a", 5, CUA_SO));
    }
}

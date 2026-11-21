package com.techgear.khuyenmai;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class MaGiamGiaTest {

    static final OffsetDateTime BAY_GIO = OffsetDateTime.of(2026, 11, 15, 10, 0, 0, 0, ZoneOffset.ofHours(7));

    static MaGiamGia ma(String loai, long giaTri, long donToiThieu, Long giamToiDa, Integer tongLuot,
                        Integer luotMoiNguoi, int daDung, boolean kichHoat) {
        return new MaGiamGia(1L, "GIAM", loai, giaTri, donToiThieu, giamToiDa, tongLuot, luotMoiNguoi, daDung,
                BAY_GIO.minusDays(1), BAY_GIO.plusDays(1), kichHoat);
    }

    @Test
    void phanTramLamTronXuongVaKhongVuotGiamToiDa() {
        assertThat(ma("phan_tram", 10, 0, null, null, null, 0, true).tienGiam(199_999)).isEqualTo(19_999);
        assertThat(ma("phan_tram", 10, 0, 50_000L, null, null, 0, true).tienGiam(1_000_000)).isEqualTo(50_000);
        assertThat(ma("phan_tram", 100, 0, null, null, null, 0, true).tienGiam(300_000)).isEqualTo(300_000);
    }

    @Test
    void soTienKhongVuotTamTinh() {
        assertThat(ma("so_tien", 50_000, 0, null, null, null, 0, true).tienGiam(600_000)).isEqualTo(50_000);
        assertThat(ma("so_tien", 50_000, 0, null, null, null, 0, true).tienGiam(30_000)).isEqualTo(30_000);
    }

    @Test
    void tienLonKhongTranSo() {
        assertThat(ma("phan_tram", 99, 0, null, null, null, 0, true).tienGiam(9_000_000_000_000_000L))
                .isEqualTo(8_910_000_000_000_000L);
    }

    @Test
    void dieuKienApDung() {
        MaGiamGia hopLe = ma("phan_tram", 10, 500_000, null, 10, 2, 9, true);
        assertThat(hopLe.loiKhiApDung(BAY_GIO, 500_000, 1)).isNull();
        assertThat(ma("phan_tram", 10, 0, null, null, null, 0, false).loiKhiApDung(BAY_GIO, 1, 0))
                .isEqualTo("Mã giảm giá đã ngừng áp dụng");
        assertThat(hopLe.loiKhiApDung(BAY_GIO.minusDays(2), 500_000, 0))
                .isEqualTo("Mã giảm giá chưa đến thời gian sử dụng");
        assertThat(hopLe.loiKhiApDung(BAY_GIO.plusDays(1), 500_000, 0)).isEqualTo("Mã giảm giá đã hết hạn");
        assertThat(hopLe.loiKhiApDung(BAY_GIO, 499_999, 0)).isEqualTo("Đơn hàng tối thiểu 500.000đ để dùng mã này");
        assertThat(ma("phan_tram", 10, 0, null, 10, null, 10, true).loiKhiApDung(BAY_GIO, 1, 0))
                .isEqualTo("Mã giảm giá đã hết lượt sử dụng");
        assertThat(hopLe.loiKhiApDung(BAY_GIO, 500_000, 2)).isEqualTo("Bạn đã dùng hết lượt của mã giảm giá này");
    }
}

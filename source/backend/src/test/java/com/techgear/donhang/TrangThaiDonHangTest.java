package com.techgear.donhang;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Random;

import static com.techgear.donhang.TrangThaiDonHang.cho_xac_nhan;
import static com.techgear.donhang.TrangThaiDonHang.da_huy;
import static com.techgear.donhang.TrangThaiDonHang.da_xac_nhan;
import static com.techgear.donhang.TrangThaiDonHang.dang_giao;
import static com.techgear.donhang.TrangThaiDonHang.hoan_thanh;
import static org.assertj.core.api.Assertions.assertThat;

class TrangThaiDonHangTest {

    @Test
    void chiChuyenTheoThuTuVaHuyTruocKhiGiao() {
        assertThat(cho_xac_nhan.coTheChuyenSang(da_xac_nhan)).isTrue();
        assertThat(cho_xac_nhan.coTheChuyenSang(da_huy)).isTrue();
        assertThat(da_xac_nhan.coTheChuyenSang(dang_giao)).isTrue();
        assertThat(da_xac_nhan.coTheChuyenSang(da_huy)).isTrue();
        assertThat(dang_giao.coTheChuyenSang(hoan_thanh)).isTrue();

        assertThat(cho_xac_nhan.coTheChuyenSang(dang_giao)).isFalse();
        assertThat(dang_giao.coTheChuyenSang(da_huy)).isFalse();
        assertThat(hoan_thanh.coTheChuyenSang(da_huy)).isFalse();
        assertThat(da_huy.coTheChuyenSang(cho_xac_nhan)).isFalse();
        assertThat(da_xac_nhan.coTheChuyenSang(cho_xac_nhan)).isFalse();
    }

    @Test
    void maDonHangDungDinhDang() {
        String ma = DonHangService.taoMaDonHang(LocalDate.of(2026, 11, 5), new Random(1));
        assertThat(ma).matches("DH261105[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
    }
}

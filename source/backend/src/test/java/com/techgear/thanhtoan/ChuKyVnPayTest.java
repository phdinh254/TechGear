package com.techgear.thanhtoan;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChuKyVnPayTest {

    static final String KHOA = "khoa-bi-mat-test";

    static Map<String, String> thamSo() {
        Map<String, String> m = new HashMap<>();
        m.put("vnp_TxnRef", "DH261115ABCDEF01");
        m.put("vnp_Amount", "1000000");
        m.put("vnp_OrderInfo", "Thanh toan don hang DH261115ABCDEF");
        return m;
    }

    @Test
    void chuoiKyXepTheoTenMaHoaGiaTriNhuMauJavaCuaVnPay() {
        assertThat(ChuKyVnPay.chuoiKy(thamSo()))
                .isEqualTo("vnp_Amount=1000000&vnp_OrderInfo=Thanh+toan+don+hang+DH261115ABCDEF&vnp_TxnRef=DH261115ABCDEF01");
    }

    @Test
    void hmacSha512KhopGiaTriTinhDocLap() {
        // tính bằng Python: hmac.new(b"khoa-bi-mat-test", chuoi_ky, hashlib.sha512).hexdigest()
        assertThat(ChuKyVnPay.ky(thamSo(), KHOA)).isEqualTo(
                "37013423302ad9bfded66ef482349456d31da4b4eb45eccd885009ea565e4209"
                        + "f378504f9abdbb7fd15ab21c57bbcdce28c8f8ff8b8c52ee128fcdc684169485");
    }

    @Test
    void kiemChuKyBoQuaTruongChuKyVaKhongPhanBietHoaThuong() {
        Map<String, String> m = thamSo();
        m.put("vnp_SecureHashType", "HmacSHA512");
        m.put("vnp_SecureHash", ChuKyVnPay.ky(thamSo(), KHOA).toUpperCase());
        assertThat(ChuKyVnPay.hopLe(m, KHOA)).isTrue();

        m.put("vnp_Amount", "2000000");
        assertThat(ChuKyVnPay.hopLe(m, KHOA)).isFalse();
        assertThat(ChuKyVnPay.hopLe(thamSo(), KHOA)).isFalse();
        assertThat(ChuKyVnPay.hopLe(m, "")).isFalse();
    }
}

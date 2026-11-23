package com.techgear.thanhtoan;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChuKyPayOsTest {

    static final String KHOA = "khoa-kiem-thu-payos";

    static String hmac(String chuoi, String khoa) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(khoa.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(chuoi.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void kyTaoLink_dungChuoiTaiLieu() throws Exception {
        String chuKy = ChuKyPayOs.kyTaoLink(2000, "https://a/c", "TG4301", 4301, "https://a/r", KHOA);
        assertThat(chuKy).isEqualTo(
                hmac("amount=2000&cancelUrl=https://a/c&description=TG4301&orderCode=4301&returnUrl=https://a/r", KHOA));
    }

    @Test
    void ky_laHmacSha256Hex() throws Exception {
        assertThat(ChuKyPayOs.ky("abc", "k")).isEqualTo(hmac("abc", "k"));
    }

    @Test
    void chuoiDuLieu_xepKhoaVaNullThanhRong() {
        Map<String, Object> data = new HashMap<>();
        data.put("orderCode", 4301);
        data.put("amount", 2000L);
        data.put("desc", "ok");
        data.put("counterAccountName", null);
        data.put("virtualAccountName", "null");
        assertThat(ChuKyPayOs.chuoiDuLieu(data))
                .isEqualTo("amount=2000&counterAccountName=&desc=ok&orderCode=4301&virtualAccountName=");
    }

    @Test
    void hopLe_chuKyDung() throws Exception {
        Map<String, Object> data = Map.of("orderCode", 4301, "amount", 2000, "code", "00");
        String dung = hmac("amount=2000&code=00&orderCode=4301", KHOA);
        assertThat(ChuKyPayOs.hopLe(data, dung, KHOA)).isTrue();
        assertThat(ChuKyPayOs.hopLe(data, dung.toUpperCase(), KHOA)).isTrue();
    }

    @Test
    void hopLe_saiChuKyHoacKhoaRong() throws Exception {
        Map<String, Object> data = Map.of("orderCode", 4301, "amount", 2000, "code", "00");
        String dung = hmac("amount=2000&code=00&orderCode=4301", KHOA);
        String sai = (dung.charAt(0) == 'a' ? "b" : "a") + dung.substring(1);
        assertThat(ChuKyPayOs.hopLe(data, sai, KHOA)).isFalse();
        assertThat(ChuKyPayOs.hopLe(data, dung, "")).isFalse();
        assertThat(ChuKyPayOs.hopLe(data, null, KHOA)).isFalse();
    }
}

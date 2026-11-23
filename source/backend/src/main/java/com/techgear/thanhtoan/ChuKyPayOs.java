package com.techgear.thanhtoan;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Chữ ký PayOS (https://payos.vn/docs/tich-hop-webhook/kiem-tra-du-lieu-voi-signature/): HMAC-SHA256 bằng checksum key, hex.
 * Tạo link ký chuỗi cố định 5 trường; webhook ký các trường của {@code data} xếp theo tên, null → chuỗi rỗng.
 */
final class ChuKyPayOs {

    private ChuKyPayOs() {
    }

    static String kyTaoLink(long amount, String cancelUrl, String description, long orderCode, String returnUrl, String khoa) {
        return ky("amount=" + amount + "&cancelUrl=" + cancelUrl + "&description=" + description
                + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl, khoa);
    }

    /** "k1=v1&k2=v2…" với khóa tăng dần; null, "null", "undefined" thành rỗng. */
    static String chuoiDuLieu(Map<String, ?> data) {
        StringJoiner sj = new StringJoiner("&");
        new TreeMap<>(data).forEach((k, v) -> sj.add(k + "=" + giaTri(v)));
        return sj.toString();
    }

    static String ky(String chuoi, String khoa) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(khoa.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(chuoi.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không ký được dữ liệu PayOS", e);
        }
    }

    /** So chữ ký nhận được với chữ ký tự tính trên {@code data} (thời gian hằng, không phân biệt hoa thường). */
    static boolean hopLe(Map<String, ?> data, String chuKy, String khoa) {
        if (data == null || chuKy == null || khoa == null || khoa.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(ky(chuoiDuLieu(data), khoa).getBytes(StandardCharsets.US_ASCII),
                chuKy.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }

    private static String giaTri(Object v) {
        if (v == null) {
            return "";
        }
        String s = v.toString();
        return "null".equals(s) || "undefined".equals(s) ? "" : s;
    }
}

package com.techgear.thanhtoan;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Chữ ký VNPay 2.1.0 theo mẫu Java chính thức (https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html):
 * tham số vnp_* khác rỗng xếp theo tên, chuỗi ký "ten=URLEncoder(giá trị, US-ASCII)" nối bằng "&", HMAC-SHA512.
 */
final class ChuKyVnPay {

    private static final String CHU_KY = "vnp_SecureHash";
    private static final String LOAI_CHU_KY = "vnp_SecureHashType";

    private ChuKyVnPay() {
    }

    static String chuoiKy(Map<String, String> thamSo) {
        StringJoiner sj = new StringJoiner("&");
        sapXep(thamSo).forEach((k, v) -> sj.add(k + "=" + maHoa(v)));
        return sj.toString();
    }

    /** Query string gửi VNPay: mã hóa cả tên và giá trị (như mẫu Java). */
    static String query(Map<String, String> thamSo) {
        StringJoiner sj = new StringJoiner("&");
        sapXep(thamSo).forEach((k, v) -> sj.add(maHoa(k) + "=" + maHoa(v)));
        return sj.toString();
    }

    static String ky(Map<String, String> thamSo, String khoa) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(khoa.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(chuoiKy(thamSo).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không ký được dữ liệu VNPay", e);
        }
    }

    /** So chữ ký nhận được với chữ ký tự tính (so sánh thời gian hằng, không phân biệt hoa thường). */
    static boolean hopLe(Map<String, String> thamSo, String khoa) {
        String nhan = thamSo.get(CHU_KY);
        if (nhan == null || khoa == null || khoa.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(ky(thamSo, khoa).getBytes(StandardCharsets.US_ASCII),
                nhan.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    private static Map<String, String> sapXep(Map<String, String> thamSo) {
        Map<String, String> ds = new TreeMap<>();
        thamSo.forEach((k, v) -> {
            if (k.startsWith("vnp_") && !k.equals(CHU_KY) && !k.equals(LOAI_CHU_KY) && v != null && !v.isEmpty()) {
                ds.put(k, v);
            }
        });
        return ds;
    }

    private static String maHoa(String s) {
        return URLEncoder.encode(s, StandardCharsets.US_ASCII);
    }
}

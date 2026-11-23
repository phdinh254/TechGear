package com.techgear.thanhtoan;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình PayOS (kênh thanh toán tạo ở my.payos.vn). {@code giaLap} chỉ bật ở profile dev khi chưa có khóa thật:
 * không gọi PayOS, link thanh toán dẫn thẳng về trang kết quả và luôn báo đã thanh toán.
 */
@ConfigurationProperties("app.payos")
public record PayOsProperties(String clientId, String apiKey, String checksumKey, String apiUrl,
                              String returnUrl, String cancelUrl, boolean giaLap) {

    boolean coKhoa() {
        return coGiaTri(clientId) && coGiaTri(apiKey) && coGiaTri(checksumKey);
    }

    /** Giả lập khi được bật và chưa đặt đủ khóa thật – có khóa thì luôn gọi PayOS thật. */
    boolean dungGiaLap() {
        return giaLap && !coKhoa();
    }

    boolean daCauHinh() {
        return dungGiaLap() || coKhoa();
    }

    private static boolean coGiaTri(String s) {
        return s != null && !s.isBlank();
    }
}

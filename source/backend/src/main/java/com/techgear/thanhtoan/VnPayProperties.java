package com.techgear.thanhtoan;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình VNPay sandbox 2.1.0; thiếu mã website hoặc khóa thì không tạo được URL thanh toán (503). */
@ConfigurationProperties("app.vnpay")
public record VnPayProperties(String tmnCode, String hashSecret, String payUrl, String returnUrl) {

    boolean daCauHinh() {
        return tmnCode != null && !tmnCode.isBlank() && hashSecret != null && !hashSecret.isBlank();
    }
}

package com.techgear.chung.baomat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** Cấu hình JWT; thiếu hoặc khóa quá ngắn thì ứng dụng không khởi động. */
@Validated
@ConfigurationProperties("app.jwt")
public record JwtProperties(
        @NotNull @Size(min = 32, message = "JWT_SECRET phải có ít nhất 32 ký tự") String khoaBiMat,
        @NotNull Duration thoiHan
) {
}

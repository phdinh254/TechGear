package com.techgear.dto;

import jakarta.validation.constraints.NotBlank;

public record DangNhapRequest(
        @NotBlank(message = "Vui lòng nhập email")
        String email,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        String matKhau
) {
}

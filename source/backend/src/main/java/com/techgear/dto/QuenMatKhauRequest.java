package com.techgear.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record QuenMatKhauRequest(
        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        String email
) {
}

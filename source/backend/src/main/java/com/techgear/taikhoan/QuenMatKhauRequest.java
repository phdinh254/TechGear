package com.techgear.taikhoan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record QuenMatKhauRequest(
        @NotBlank(message = "Vui lòng nhập email") @Email(message = "Email không đúng định dạng") String email
) {
}

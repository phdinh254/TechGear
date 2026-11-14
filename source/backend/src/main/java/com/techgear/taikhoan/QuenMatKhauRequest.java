package com.techgear.taikhoan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuenMatKhauRequest(
        @NotBlank(message = "Vui lòng nhập email") @Email(message = "Email không đúng định dạng")
        @Size(max = 150, message = "Email tối đa 150 ký tự") String email
) {
}

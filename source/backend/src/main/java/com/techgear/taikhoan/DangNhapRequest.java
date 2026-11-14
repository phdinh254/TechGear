package com.techgear.taikhoan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DangNhapRequest(
        @NotBlank(message = "Vui lòng nhập email") @Size(max = 150, message = "Email tối đa 150 ký tự") String email,
        @NotBlank(message = "Vui lòng nhập mật khẩu") @Size(max = 200, message = "Mật khẩu quá dài") String matKhau
) {
}

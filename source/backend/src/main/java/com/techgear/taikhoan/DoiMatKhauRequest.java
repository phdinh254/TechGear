package com.techgear.taikhoan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoiMatKhauRequest(
        @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
        @Size(max = 200, message = "Mật khẩu quá dài") String matKhauHienTai,
        @MatKhauHopLe String matKhauMoi
) {
}

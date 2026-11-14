package com.techgear.taikhoan;

import jakarta.validation.constraints.NotBlank;

public record DoiMatKhauRequest(
        @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại") String matKhauHienTai,
        @MatKhauHopLe String matKhauMoi
) {
}

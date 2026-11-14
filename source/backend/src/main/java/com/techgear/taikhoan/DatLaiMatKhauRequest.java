package com.techgear.taikhoan;

import jakarta.validation.constraints.NotBlank;

public record DatLaiMatKhauRequest(
        @NotBlank(message = "Thiếu mã đặt lại mật khẩu") String ma,
        @MatKhauHopLe String matKhauMoi
) {
}

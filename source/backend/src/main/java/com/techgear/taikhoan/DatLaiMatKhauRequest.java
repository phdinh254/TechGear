package com.techgear.taikhoan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatLaiMatKhauRequest(
        @NotBlank(message = "Thiếu mã đặt lại mật khẩu") @Size(max = 100, message = "Mã không hợp lệ") String ma,
        @MatKhauHopLe String matKhauMoi
) {
}

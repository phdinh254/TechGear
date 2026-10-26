package com.techgear.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatLaiMatKhauRequest(
        @NotBlank(message = "Thiếu mã đặt lại mật khẩu")
        @Size(max = 200, message = "Mã không hợp lệ")
        String ma,

        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @Size(min = 6, max = 100, message = "Mật khẩu từ 6 đến 100 ký tự")
        String matKhauMoi
) {
}

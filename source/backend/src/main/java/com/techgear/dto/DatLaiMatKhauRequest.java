package com.techgear.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DatLaiMatKhauRequest(
        @NotBlank(message = "Thiếu mã đặt lại mật khẩu")
        @Size(max = 200, message = "Mã không hợp lệ")
        String ma,

        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @Size(min = 8, max = 100, message = "Mật khẩu từ 8 đến 100 ký tự")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,100}$",
                message = "Mật khẩu phải có chữ hoa, chữ thường và chữ số")
        String matKhauMoi
) {
}

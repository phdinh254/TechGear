package com.techgear.taikhoan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DangKyRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String hoTen,

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 150, message = "Email tối đa 150 ký tự")
        String email,

        @MatKhauHopLe
        String matKhau,

        /** Không bắt buộc – frontend gửi chuỗi rỗng khi bỏ trống. */
        @Pattern(regexp = SO_DIEN_THOAI_HOAC_RONG, message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
        String soDienThoai
) {
    static final String SO_DIEN_THOAI_HOAC_RONG = "(0\\d{9})?";
}

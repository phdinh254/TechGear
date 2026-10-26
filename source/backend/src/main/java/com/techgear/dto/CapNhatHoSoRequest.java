package com.techgear.dto;

import com.techgear.entity.enums.GioiTinh;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CapNhatHoSoRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String hoTen,

        @Pattern(regexp = "^$|^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
        String soDienThoai,

        GioiTinh gioiTinh,

        @Past(message = "Ngày sinh phải trước ngày hiện tại")
        LocalDate ngaySinh
) {
}

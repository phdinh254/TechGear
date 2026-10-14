package com.techgear.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ThuongHieuRequest(
        @NotBlank(message = "Vui lòng nhập tên thương hiệu")
        @Size(max = 100, message = "Tên tối đa 100 ký tự")
        String ten,

        @Size(max = 255)
        String logo,

        String moTa,

        Boolean hienThi
) {
}

package com.techgear.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminDanhMucRequest(
        @NotBlank(message = "Vui lòng nhập tên danh mục")
        @Size(max = 100, message = "Tên tối đa 100 ký tự")
        String ten,

        Long danhMucChaId,

        String moTa,

        @Size(max = 255, message = "Đường dẫn hình ảnh tối đa 255 ký tự")
        String hinhAnh,

        @NotNull(message = "Vui lòng nhập thứ tự")
        @Min(value = 0, message = "Thứ tự không được âm")
        Integer thuTu,

        /** null = hiển thị. */
        Boolean hienThi
) {
}

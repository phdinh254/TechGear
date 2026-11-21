package com.techgear.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NhapKhoRequest(
        @NotNull(message = "Vui lòng nhập số lượng")
        @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
        @Max(value = 100000, message = "Số lượng nhập tối đa 100000")
        Integer soLuong,
        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
        String ghiChu
) {
}

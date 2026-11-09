package com.techgear.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CapNhatGioHangRequest(
        @NotNull(message = "Vui lòng nhập số lượng")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        Integer soLuong
) {
}

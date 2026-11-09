package com.techgear.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GioHangItemRequest(
        @NotNull(message = "Vui lòng chọn biến thể")
        @Positive(message = "Biến thể không hợp lệ")
        Long bienTheId,

        @NotNull(message = "Vui lòng nhập số lượng")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        Integer soLuong
) {
}

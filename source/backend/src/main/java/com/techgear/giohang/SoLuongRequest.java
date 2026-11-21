package com.techgear.giohang;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SoLuongRequest(
        @NotNull(message = "Số lượng từ 1 đến 999") @Min(value = 1, message = "Số lượng từ 1 đến 999")
        @Max(value = 999, message = "Số lượng từ 1 đến 999") Integer soLuong
) {
}

package com.techgear.giohang;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Thêm vào giỏ, và mỗi phần tử của giỏ tạm khi gộp. */
public record DongGioRequest(
        @NotNull(message = "Thiếu phiên bản sản phẩm") Long bienTheId,
        @NotNull(message = "Số lượng từ 1 đến 999") @Min(value = 1, message = "Số lượng từ 1 đến 999")
        @Max(value = 999, message = "Số lượng từ 1 đến 999") Integer soLuong
) {
}

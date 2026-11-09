package com.techgear.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DanhGiaRequest(
        @NotNull(message = "Vui lòng chọn đơn hàng") @Positive Long donHangId,
        @NotNull(message = "Vui lòng chọn sản phẩm") @Positive Long sanPhamId,
        @NotNull(message = "Vui lòng chọn số sao")
        @Min(value = 1, message = "Số sao tối thiểu là 1")
        @Max(value = 5, message = "Số sao tối đa là 5")
        Integer soSao,
        @Size(max = 2000, message = "Nội dung đánh giá tối đa 2000 ký tự")
        String noiDung
) {
}

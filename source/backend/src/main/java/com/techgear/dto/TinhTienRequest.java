package com.techgear.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TinhTienRequest(
        @Size(max = 50, message = "Mã giảm giá tối đa 50 ký tự")
        String maGiamGia,

        @Size(max = 100, message = "Giỏ hàng tối đa 100 dòng")
        List<@Valid GioHangItemRequest> items
) {
}

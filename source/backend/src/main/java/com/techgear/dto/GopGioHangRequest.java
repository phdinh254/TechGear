package com.techgear.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GopGioHangRequest(
        @NotNull(message = "Danh sách giỏ hàng không được để trống")
        @Size(max = 100, message = "Giỏ hàng tối đa 100 dòng")
        List<@Valid GioHangItemRequest> items
) {
}

package com.techgear.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DieuChinhKhoRequest(
        @NotNull(message = "Vui lòng nhập số lượng thực tế")
        @Min(value = 0, message = "Số lượng thực tế không được âm")
        Integer soLuongThucTe,
        @NotBlank(message = "Vui lòng nhập lý do điều chỉnh")
        @Size(max = 255, message = "Lý do tối đa 255 ký tự")
        String lyDo
) {
}

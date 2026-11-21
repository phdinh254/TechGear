package com.techgear.dto.admin;

import jakarta.validation.constraints.NotNull;

public record HienThiRequest(@NotNull(message = "Vui lòng chọn trạng thái hiển thị") Boolean hienThi) {
}

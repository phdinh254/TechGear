package com.techgear.dto;

import jakarta.validation.constraints.Size;

public record HuyDonRequest(
        @Size(max = 500, message = "Lý do hủy tối đa 500 ký tự")
        String lyDo
) {
}

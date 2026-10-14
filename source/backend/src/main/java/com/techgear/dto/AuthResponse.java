package com.techgear.dto;

public record AuthResponse(
        String token,
        long thoiHanMs,
        NguoiDungResponse nguoiDung
) {
}

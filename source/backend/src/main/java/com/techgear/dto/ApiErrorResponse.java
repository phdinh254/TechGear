package com.techgear.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** Cấu trúc lỗi dùng thống nhất cho controller và Spring Security. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        LocalDateTime thoiGian,
        int trangThai,
        String thongBao,
        Map<String, String> loi
) {
    public static ApiErrorResponse tao(int trangThai, String thongBao) {
        return tao(trangThai, thongBao, null);
    }

    public static ApiErrorResponse tao(int trangThai, String thongBao, Map<String, String> loi) {
        return new ApiErrorResponse(LocalDateTime.now(), trangThai, thongBao, loi);
    }
}

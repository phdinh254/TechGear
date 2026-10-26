package com.techgear.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/** Định dạng lỗi thống nhất trả về client: { thoiGian, trangThai, thongBao, loi } */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String thoiGian,
        int trangThai,
        String thongBao,
        Object loi
) {
    public static ApiError of(int trangThai, String thongBao, Object loi) {
        return new ApiError(LocalDateTime.now().toString(), trangThai, thongBao, loi);
    }
}

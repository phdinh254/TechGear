package com.techgear.chung.loi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.ThoiGian;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/** Định dạng lỗi thống nhất của mọi API: { thoiGian, trangThai, thongBao, loi }. */
public record ApiError(
        OffsetDateTime thoiGian,
        int trangThai,
        String thongBao,
        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> loi
) {
    public static ApiError tao(HttpStatus trangThai, String thongBao) {
        return new ApiError(ThoiGian.bayGio(), trangThai.value(), thongBao, null);
    }

    public static ApiError tao(HttpStatus trangThai, String thongBao, Map<String, String> loi) {
        return new ApiError(ThoiGian.bayGio(), trangThai.value(), thongBao, loi);
    }
}

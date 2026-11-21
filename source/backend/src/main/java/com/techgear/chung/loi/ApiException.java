package com.techgear.chung.loi;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ có mã HTTP và thông báo tiếng Việt hiển thị cho người dùng. */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus trangThai;

    public ApiException(HttpStatus trangThai, String thongBao) {
        super(thongBao);
        this.trangThai = trangThai;
    }

    public static ApiException yeuCauKhongHopLe(String thongBao) {
        return new ApiException(HttpStatus.BAD_REQUEST, thongBao);
    }

    public static ApiException chuaXacThuc(String thongBao) {
        return new ApiException(HttpStatus.UNAUTHORIZED, thongBao);
    }

    public static ApiException khongCoQuyen(String thongBao) {
        return new ApiException(HttpStatus.FORBIDDEN, thongBao);
    }

    public static ApiException khongTimThay(String thongBao) {
        return new ApiException(HttpStatus.NOT_FOUND, thongBao);
    }

    public static ApiException xungDot(String thongBao) {
        return new ApiException(HttpStatus.CONFLICT, thongBao);
    }

    public static ApiException quaNhieuYeuCau(String thongBao) {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, thongBao);
    }
}

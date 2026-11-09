package com.techgear.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ, trả về cho client kèm mã HTTP tương ứng. */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static ApiException khongTimThay(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException yeuCauKhongHopLe(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException xungDot(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException khongCoQuyen(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }
}

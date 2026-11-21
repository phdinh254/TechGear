package com.techgear.chung.loi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Chuyển mọi lỗi thành {@link ApiError} tiếng Việt; không lộ chi tiết nội bộ ra ngoài. */
@Slf4j
@RestControllerAdvice
public class XuLyLoiToanCuc {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> apiException(ApiException e) {
        return tra(e.getTrangThai(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> duLieuKhongHopLe(MethodArgumentNotValidException e) {
        Map<String, String> loi = new LinkedHashMap<>();
        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            loi.putIfAbsent(f.getField(), f.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ApiError.tao(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", loi));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> thamSoKhongHopLe(HandlerMethodValidationException e) {
        Map<String, String> loi = new LinkedHashMap<>();
        e.getParameterValidationResults().forEach(r -> r.getResolvableErrors().forEach(
                err -> loi.putIfAbsent(r.getMethodParameter().getParameterName(), err.getDefaultMessage())));
        return ResponseEntity.badRequest().body(ApiError.tao(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", loi));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> jsonHong() {
        return tra(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không đúng định dạng");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> saiKieu(MethodArgumentTypeMismatchException e) {
        return tra(HttpStatus.BAD_REQUEST, "Tham số '" + e.getName() + "' không hợp lệ");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> thieuThamSo(MissingServletRequestParameterException e) {
        return tra(HttpStatus.BAD_REQUEST, "Thiếu tham số '" + e.getParameterName() + "'");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity<ApiError> thieuFile(MissingServletRequestPartException e) {
        return tra(HttpStatus.BAD_REQUEST, "Thiếu tệp '" + e.getRequestPartName() + "'");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> saiPhuongThuc(HttpRequestMethodNotSupportedException e) {
        return tra(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức " + e.getMethod() + " không được hỗ trợ");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> saiKieuNoiDung() {
        return tra(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Kiểu dữ liệu gửi lên không được hỗ trợ");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> fileQuaLon() {
        return tra(HttpStatus.CONTENT_TOO_LARGE, "Tệp tải lên vượt quá dung lượng cho phép (5MB)");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> khongCoDuongDan() {
        return tra(HttpStatus.NOT_FOUND, "Không tìm thấy đường dẫn");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> khongCoQuyen() {
        return tra(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> viPhamRangBuoc(DataIntegrityViolationException e) {
        log.warn("Vi phạm ràng buộc CSDL: {}", e.getMostSpecificCause().getMessage());
        return tra(HttpStatus.CONFLICT, "Dữ liệu bị trùng hoặc đang được sử dụng");
    }

    /** Các lỗi còn lại của Spring MVC đã mang sẵn mã HTTP (406, ResponseStatusException...): giữ đúng mã. */
    @ExceptionHandler(ErrorResponseException.class)
    ResponseEntity<ApiError> loiCoMa(ErrorResponseException e) {
        HttpStatus trangThai = HttpStatus.valueOf(e.getStatusCode().value());
        return tra(trangThai, trangThai.is5xxServerError()
                ? "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau" : "Yêu cầu không thể xử lý");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    ResponseEntity<Void> khongTraDuocDinhDang() {
        // client không nhận JSON nên không gửi thân lỗi được
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> loiHeThong(Exception e) {
        log.error("Lỗi không lường trước", e);
        return tra(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau");
    }

    private static ResponseEntity<ApiError> tra(HttpStatus trangThai, String thongBao) {
        return ResponseEntity.status(trangThai).body(ApiError.tao(trangThai, thongBao));
    }
}

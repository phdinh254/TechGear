package com.techgear.exception;

import com.techgear.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Chuyển mọi lỗi thành JSON thống nhất: { thoiGian, trangThai, thongBao, loi } */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> xuLyApiException(ApiException ex) {
        return taoPhanHoi(ex.getStatus(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> xuLyLoiKiemTra(MethodArgumentNotValidException ex) {
        Map<String, String> loi = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> loi.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", loi);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> xuLyLoiThamSo(ConstraintViolationException ex) {
        Map<String, String> loi = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(viPham -> {
            String duongDan = viPham.getPropertyPath().toString();
            String tenTruong = duongDan.substring(duongDan.lastIndexOf('.') + 1);
            loi.putIfAbsent(tenTruong, viPham.getMessage());
        });
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", loi);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> xuLyKhongCoQuyen(AccessDeniedException ex) {
        return taoPhanHoi(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này", null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> xuLyDuLieuSaiDinhDang(Exception ex) {
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên sai định dạng", null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> xuLyTepQuaLon(MaxUploadSizeExceededException ex) {
        return taoPhanHoi(HttpStatus.PAYLOAD_TOO_LARGE, "Tệp tải lên vượt quá dung lượng cho phép", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> xuLyKhongTimThayDuongDan(NoResourceFoundException ex) {
        return taoPhanHoi(HttpStatus.NOT_FOUND, "Không tìm thấy đường dẫn", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> xuLySaiPhuongThuc(HttpRequestMethodNotSupportedException ex) {
        return taoPhanHoi(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức HTTP không được hỗ trợ", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> xuLyLoiKhac(Exception ex) {
        log.error("Lỗi không xác định", ex);
        return taoPhanHoi(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống", null);
    }

    private ResponseEntity<ApiErrorResponse> taoPhanHoi(HttpStatus status, String thongBao,
                                                         Map<String, String> loi) {
        return ResponseEntity.status(status).body(ApiErrorResponse.tao(status.value(), thongBao, loi));
    }
}

package com.techgear.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
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

/** Chuyển mọi lỗi thành JSON thống nhất: { thoiGian, trangThai, thongBao, loi } */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> xuLyApiException(ApiException ex) {
        return taoPhanHoi(ex.getStatus(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> xuLyLoiKiemTra(MethodArgumentNotValidException ex) {
        Map<String, String> loi = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> loi.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> loi.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", loi);
    }

    /** Lỗi kiểm tra tham số đường dẫn / query (@Min, @Max... đặt trực tiếp trên tham số). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> xuLyLoiKiemTraThamSo(HandlerMethodValidationException ex) {
        Map<String, String> loi = new LinkedHashMap<>();
        ex.getAllValidationResults().forEach(r -> r.getResolvableErrors().forEach(e ->
                loi.putIfAbsent(r.getMethodParameter().getParameterName(), e.getDefaultMessage())));
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", loi);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> xuLyRangBuoc(ConstraintViolationException ex) {
        Map<String, String> loi = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String duongDan = v.getPropertyPath().toString();
            loi.putIfAbsent(duongDan.substring(duongDan.lastIndexOf('.') + 1), v.getMessage());
        });
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", loi);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> xuLyKhongCoQuyen(AccessDeniedException ex) {
        return taoPhanHoi(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này", null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> xuLyDuLieuSaiDinhDang(Exception ex) {
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên sai định dạng", null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> xuLyThieuThamSo(MissingServletRequestParameterException ex) {
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Thiếu tham số bắt buộc: " + ex.getParameterName(), null);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> xuLyThieuFile(MissingServletRequestPartException ex) {
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu bắt buộc: " + ex.getRequestPartName(), null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> xuLyFileQuaLon(MaxUploadSizeExceededException ex) {
        return taoPhanHoi(HttpStatus.PAYLOAD_TOO_LARGE, "File tải lên vượt quá dung lượng cho phép (tối đa 5MB)", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> xuLySaiPhuongThuc(HttpRequestMethodNotSupportedException ex) {
        return taoPhanHoi(HttpStatus.METHOD_NOT_ALLOWED,
                "Phương thức " + ex.getMethod() + " không được hỗ trợ cho đường dẫn này", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> xuLySaiKieuDuLieu(HttpMediaTypeNotSupportedException ex) {
        return taoPhanHoi(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Kiểu dữ liệu gửi lên không được hỗ trợ", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> xuLyKhongTimThayDuongDan(NoResourceFoundException ex) {
        return taoPhanHoi(HttpStatus.NOT_FOUND, "Không tìm thấy đường dẫn", null);
    }

    /** Vi phạm ràng buộc CSDL (UNIQUE, khóa ngoại, CHECK) mà tầng nghiệp vụ chưa chặn trước. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> xuLyViPhamRangBuoc(DataIntegrityViolationException ex) {
        log.warn("Vi phạm ràng buộc dữ liệu: {}", ex.getMostSpecificCause().getMessage());
        return taoPhanHoi(HttpStatus.CONFLICT,
                "Dữ liệu bị trùng hoặc đang được sử dụng ở nơi khác, không thể lưu", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> xuLyLoiKhac(Exception ex) {
        log.error("Lỗi không xác định", ex);
        return taoPhanHoi(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống", null);
    }

    private ResponseEntity<ApiError> taoPhanHoi(HttpStatus status, String thongBao, Object loi) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), thongBao, loi));
    }
}

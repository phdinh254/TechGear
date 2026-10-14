package com.techgear.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Chuyển mọi lỗi thành JSON thống nhất: { thoiGian, trangThai, thongBao, loi } */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> xuLyApiException(ApiException ex) {
        return taoPhanHoi(ex.getStatus(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> xuLyLoiKiemTra(MethodArgumentNotValidException ex) {
        Map<String, String> loi = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> loi.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", loi);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> xuLyKhongCoQuyen(AccessDeniedException ex) {
        return taoPhanHoi(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này", null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> xuLyDuLieuSaiDinhDang(Exception ex) {
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên sai định dạng", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> xuLyKhongTimThayDuongDan(NoResourceFoundException ex) {
        return taoPhanHoi(HttpStatus.NOT_FOUND, "Không tìm thấy đường dẫn", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> xuLyLoiKhac(Exception ex) {
        log.error("Lỗi không xác định", ex);
        return taoPhanHoi(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống", null);
    }

    private ResponseEntity<Map<String, Object>> taoPhanHoi(HttpStatus status, String thongBao, Object loi) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("thoiGian", LocalDateTime.now().toString());
        body.put("trangThai", status.value());
        body.put("thongBao", thongBao);
        if (loi != null) {
            body.put("loi", loi);
        }
        return ResponseEntity.status(status).body(body);
    }
}

package com.techgear.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgear.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Trả lỗi 401/403 của Spring Security cùng định dạng JSON với GlobalExceptionHandler. */
@Component
@RequiredArgsConstructor
public class JsonAuthHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        ghi(response, HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để thực hiện thao tác này");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        ghi(response, HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
    }

    private void ghi(HttpServletResponse response, HttpStatus status, String thongBao) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiError.of(status.value(), thongBao, null));
    }
}

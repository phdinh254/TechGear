package com.techgear.chung.baomat;

import com.techgear.chung.loi.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Trả lỗi 401/403 của Spring Security cùng định dạng JSON với XuLyLoiToanCuc. */
@Component
@RequiredArgsConstructor
public class JsonLoiBaoMat implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean coToken = header != null && header.startsWith("Bearer ");
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        ghi(response, HttpStatus.UNAUTHORIZED,
                coToken ? "Phiên đăng nhập không hợp lệ hoặc đã hết hạn" : "Vui lòng đăng nhập");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException {
        ghi(response, HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
    }

    private void ghi(HttpServletResponse response, HttpStatus trangThai, String thongBao) throws IOException {
        response.setStatus(trangThai.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), ApiError.tao(trangThai, thongBao));
    }
}

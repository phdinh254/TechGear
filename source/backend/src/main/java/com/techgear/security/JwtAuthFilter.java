package com.techgear.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Đọc header "Authorization: Bearer <token>", nếu hợp lệ thì đặt người dùng
 * vào SecurityContext. Token sai/hết hạn thì bỏ qua, request sẽ bị chặn ở
 * bước phân quyền nếu API yêu cầu đăng nhập.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String TIEN_TO = "Bearer ";

    private final JwtService jwtService;
    private final NguoiDungDetailsService nguoiDungDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(TIEN_TO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(TIEN_TO.length());
            try {
                String email = jwtService.layEmail(token);
                UserDetails user = nguoiDungDetailsService.loadUserByUsername(email);
                if (user.isEnabled()) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                // Token không hợp lệ: không xác thực, để tầng phân quyền xử lý.
            }
        }
        chain.doFilter(request, response);
    }
}

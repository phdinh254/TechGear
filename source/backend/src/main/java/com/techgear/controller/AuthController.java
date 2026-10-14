package com.techgear.controller;

import com.techgear.dto.AuthResponse;
import com.techgear.dto.DangKyRequest;
import com.techgear.dto.DangNhapRequest;
import com.techgear.dto.NguoiDungResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/dang-ky")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse dangKy(@Valid @RequestBody DangKyRequest req) {
        return authService.dangKy(req);
    }

    @PostMapping("/dang-nhap")
    public AuthResponse dangNhap(@Valid @RequestBody DangNhapRequest req) {
        return authService.dangNhap(req);
    }

    /** Thông tin người dùng đang đăng nhập (cần token). */
    @GetMapping("/toi")
    public NguoiDungResponse thongTinCuaToi(@AuthenticationPrincipal NguoiDungPrincipal nguoiDung) {
        return authService.layThongTin(nguoiDung.getId());
    }
}

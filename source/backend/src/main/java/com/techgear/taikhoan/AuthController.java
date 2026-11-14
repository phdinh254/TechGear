package com.techgear.taikhoan;

import com.techgear.chung.baomat.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final TaiKhoanService taiKhoanService;

    @PostMapping("/dang-ky")
    @ResponseStatus(HttpStatus.CREATED)
    XacThucResponse dangKy(@Valid @RequestBody DangKyRequest req) {
        return taiKhoanService.dangKy(req);
    }

    @PostMapping("/dang-nhap")
    XacThucResponse dangNhap(@Valid @RequestBody DangNhapRequest req, HttpServletRequest http) {
        return taiKhoanService.dangNhap(req, http.getRemoteAddr());
    }

    @GetMapping("/toi")
    NguoiDungResponse toi(@AuthenticationPrincipal Jwt jwt) {
        return taiKhoanService.layThongTin(JwtService.nguoiDungId(jwt));
    }
}

package com.techgear.taikhoan;

import com.techgear.chung.baomat.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tai-khoan")
@RequiredArgsConstructor
class TaiKhoanController {

    private final TaiKhoanService taiKhoanService;

    @GetMapping
    NguoiDungResponse xem(@AuthenticationPrincipal Jwt jwt) {
        return taiKhoanService.layThongTin(JwtService.nguoiDungId(jwt));
    }

    @PutMapping
    NguoiDungResponse sua(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CapNhatHoSoRequest req) {
        return taiKhoanService.capNhatHoSo(JwtService.nguoiDungId(jwt), req);
    }

    @PutMapping("/mat-khau")
    XacThucResponse doiMatKhau(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DoiMatKhauRequest req) {
        return taiKhoanService.doiMatKhau(JwtService.nguoiDungId(jwt), req);
    }

    @PostMapping("/anh-dai-dien")
    NguoiDungResponse doiAnhDaiDien(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file) {
        return taiKhoanService.doiAnhDaiDien(JwtService.nguoiDungId(jwt), file);
    }
}

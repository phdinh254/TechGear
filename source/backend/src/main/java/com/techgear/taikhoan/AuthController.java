package com.techgear.taikhoan;

import com.techgear.chung.ThongBao;
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
    private final DatLaiMatKhauService datLaiMatKhauService;

    @PostMapping("/dang-ky")
    @ResponseStatus(HttpStatus.CREATED)
    XacThucResponse dangKy(@Valid @RequestBody DangKyRequest req) {
        return taiKhoanService.dangKy(req);
    }

    @PostMapping("/dang-nhap")
    XacThucResponse dangNhap(@Valid @RequestBody DangNhapRequest req, HttpServletRequest http) {
        return taiKhoanService.dangNhap(req, http.getRemoteAddr());
    }

    @PostMapping("/quen-mat-khau")
    ThongBao quenMatKhau(@Valid @RequestBody QuenMatKhauRequest req, HttpServletRequest http) {
        datLaiMatKhauService.quenMatKhau(req.email(), http.getRemoteAddr());
        return new ThongBao("Nếu email đã đăng ký, liên kết đặt lại mật khẩu đã được gửi");
    }

    @PostMapping("/dat-lai-mat-khau")
    ThongBao datLaiMatKhau(@Valid @RequestBody DatLaiMatKhauRequest req) {
        datLaiMatKhauService.datLaiMatKhau(req);
        return new ThongBao("Đặt lại mật khẩu thành công, vui lòng đăng nhập");
    }

    @GetMapping("/toi")
    NguoiDungResponse toi(@AuthenticationPrincipal Jwt jwt) {
        return taiKhoanService.layThongTin(JwtService.nguoiDungId(jwt));
    }
}

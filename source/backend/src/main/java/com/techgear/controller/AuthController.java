package com.techgear.controller;

import com.techgear.dto.AuthResponse;
import com.techgear.dto.DangKyRequest;
import com.techgear.dto.DangNhapRequest;
import com.techgear.dto.DatLaiMatKhauRequest;
import com.techgear.dto.NguoiDungResponse;
import com.techgear.dto.QuenMatKhauRequest;
import com.techgear.dto.ThongBaoResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.AuthService;
import com.techgear.service.MatKhauService;
import com.techgear.service.TaiKhoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Xác thực")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MatKhauService matKhauService;
    private final TaiKhoanService taiKhoanService;

    @PostMapping("/dang-ky")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse dangKy(@Valid @RequestBody DangKyRequest req) {
        return authService.dangKy(req);
    }

    @PostMapping("/dang-nhap")
    public AuthResponse dangNhap(@Valid @RequestBody DangNhapRequest req) {
        return authService.dangNhap(req);
    }

    /** Thông tin người dùng đang đăng nhập (cần token). Giống GET /api/tai-khoan. */
    @GetMapping("/toi")
    public NguoiDungResponse thongTinCuaToi(@AuthenticationPrincipal NguoiDungPrincipal nguoiDung) {
        return taiKhoanService.layThongTin(nguoiDung.getId());
    }

    @Operation(summary = "Quên mật khẩu",
            description = "Gửi liên kết đặt lại mật khẩu (hiệu lực 15 phút) tới email. "
                    + "Luôn trả cùng một thông báo dù email có tồn tại hay không.")
    @PostMapping("/quen-mat-khau")
    public ThongBaoResponse quenMatKhau(@Valid @RequestBody QuenMatKhauRequest req) {
        return matKhauService.yeuCauDatLai(req.email());
    }

    @Operation(summary = "Đặt lại mật khẩu bằng mã trong liên kết email")
    @PostMapping("/dat-lai-mat-khau")
    public ThongBaoResponse datLaiMatKhau(@Valid @RequestBody DatLaiMatKhauRequest req) {
        return matKhauService.datLai(req);
    }
}

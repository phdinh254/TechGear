package com.techgear.controller;

import com.techgear.dto.CapNhatHoSoRequest;
import com.techgear.dto.DoiMatKhauRequest;
import com.techgear.dto.NguoiDungResponse;
import com.techgear.dto.ThongBaoResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.TaiKhoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Tài khoản")
@RestController
@RequestMapping("/api/tai-khoan")
@RequiredArgsConstructor
public class TaiKhoanController {

    private final TaiKhoanService taiKhoanService;

    @GetMapping
    public NguoiDungResponse layThongTin(@AuthenticationPrincipal NguoiDungPrincipal nd) {
        return taiKhoanService.layThongTin(nd.getId());
    }

    @Operation(summary = "Cập nhật họ tên, số điện thoại, giới tính, ngày sinh")
    @PutMapping
    public NguoiDungResponse capNhatHoSo(@AuthenticationPrincipal NguoiDungPrincipal nd,
                                         @Valid @RequestBody CapNhatHoSoRequest req) {
        return taiKhoanService.capNhatHoSo(nd.getId(), req);
    }

    @PutMapping("/mat-khau")
    public ThongBaoResponse doiMatKhau(@AuthenticationPrincipal NguoiDungPrincipal nd,
                                       @Valid @RequestBody DoiMatKhauRequest req) {
        return taiKhoanService.doiMatKhau(nd.getId(), req);
    }

    @Operation(summary = "Đổi ảnh đại diện (multipart, trường file; JPG/PNG/WEBP/GIF tối đa 5MB)")
    @PostMapping(value = "/anh-dai-dien", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NguoiDungResponse doiAnhDaiDien(@AuthenticationPrincipal NguoiDungPrincipal nd,
                                           @RequestPart("file") MultipartFile file) {
        return taiKhoanService.capNhatAnhDaiDien(nd.getId(), file);
    }
}

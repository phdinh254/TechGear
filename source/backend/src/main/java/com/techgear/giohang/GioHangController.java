package com.techgear.giohang;

import com.techgear.chung.baomat.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gio-hang")
@RequiredArgsConstructor
class GioHangController {

    private final GioHangService gioHangService;

    @GetMapping
    GioHangResponse xem(@AuthenticationPrincipal Jwt jwt) {
        return gioHangService.xem(JwtService.nguoiDungId(jwt));
    }

    @PostMapping
    GioHangResponse them(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DongGioRequest req) {
        return gioHangService.them(JwtService.nguoiDungId(jwt), req.bienTheId(), req.soLuong());
    }

    @PutMapping("/{bienTheId}")
    GioHangResponse sua(@AuthenticationPrincipal Jwt jwt, @PathVariable Long bienTheId,
                        @Valid @RequestBody SoLuongRequest req) {
        return gioHangService.sua(JwtService.nguoiDungId(jwt), bienTheId, req.soLuong());
    }

    @DeleteMapping("/{bienTheId}")
    GioHangResponse xoa(@AuthenticationPrincipal Jwt jwt, @PathVariable Long bienTheId) {
        return gioHangService.xoa(JwtService.nguoiDungId(jwt), bienTheId);
    }

    @PostMapping("/gop")
    GioHangResponse gop(@AuthenticationPrincipal Jwt jwt,
                        @RequestBody @Size(max = 100, message = "Giỏ tạm tối đa 100 dòng") List<@Valid DongGioRequest> gioTam) {
        return gioHangService.gop(JwtService.nguoiDungId(jwt), gioTam);
    }
}

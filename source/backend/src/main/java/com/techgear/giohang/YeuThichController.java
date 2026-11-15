package com.techgear.giohang;

import com.techgear.chung.baomat.JwtService;
import com.techgear.sanpham.SanPhamTomTat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/yeu-thich")
@RequiredArgsConstructor
class YeuThichController {

    private final YeuThichService yeuThichService;

    @GetMapping
    List<SanPhamTomTat> danhSach(@AuthenticationPrincipal Jwt jwt) {
        return yeuThichService.danhSach(JwtService.nguoiDungId(jwt));
    }

    @PutMapping("/{sanPhamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void them(@AuthenticationPrincipal Jwt jwt, @PathVariable Long sanPhamId) {
        yeuThichService.them(JwtService.nguoiDungId(jwt), sanPhamId);
    }

    @DeleteMapping("/{sanPhamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void bo(@AuthenticationPrincipal Jwt jwt, @PathVariable Long sanPhamId) {
        yeuThichService.bo(JwtService.nguoiDungId(jwt), sanPhamId);
    }
}

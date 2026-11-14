package com.techgear.taikhoan;

import com.techgear.chung.baomat.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tai-khoan/dia-chi")
@RequiredArgsConstructor
class DiaChiController {

    private final DiaChiService diaChiService;

    @GetMapping
    List<DiaChiResponse> danhSach(@AuthenticationPrincipal Jwt jwt) {
        return diaChiService.danhSach(JwtService.nguoiDungId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DiaChiResponse them(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DiaChiRequest req) {
        return diaChiService.tao(JwtService.nguoiDungId(jwt), req);
    }

    @PutMapping("/{id}")
    DiaChiResponse sua(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody DiaChiRequest req) {
        return diaChiService.sua(JwtService.nguoiDungId(jwt), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void xoa(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        diaChiService.xoa(JwtService.nguoiDungId(jwt), id);
    }

    @PatchMapping("/{id}/mac-dinh")
    DiaChiResponse datMacDinh(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return diaChiService.datMacDinh(JwtService.nguoiDungId(jwt), id);
    }
}

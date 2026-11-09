package com.techgear.controller;

import com.techgear.dto.CapNhatGioHangRequest;
import com.techgear.dto.GioHangItemRequest;
import com.techgear.dto.GioHangResponse;
import com.techgear.dto.GopGioHangRequest;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.GioHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gio-hang")
@RequiredArgsConstructor
public class GioHangController {

    private final GioHangService gioHangService;

    @GetMapping
    public GioHangResponse layGioHang(@AuthenticationPrincipal NguoiDungPrincipal principal) {
        return gioHangService.layGioHang(principal.getId());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public GioHangResponse them(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                @Valid @RequestBody GioHangItemRequest request) {
        return gioHangService.them(principal.getId(), request);
    }

    @PutMapping("/items/{bienTheId}")
    public GioHangResponse capNhat(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                   @PathVariable Long bienTheId,
                                   @Valid @RequestBody CapNhatGioHangRequest request) {
        return gioHangService.capNhat(principal.getId(), bienTheId, request.soLuong());
    }

    @DeleteMapping("/items/{bienTheId}")
    public GioHangResponse xoa(@AuthenticationPrincipal NguoiDungPrincipal principal,
                               @PathVariable Long bienTheId) {
        return gioHangService.xoa(principal.getId(), bienTheId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoaTatCa(@AuthenticationPrincipal NguoiDungPrincipal principal) {
        gioHangService.xoaTatCa(principal.getId());
    }

    @PostMapping("/gop")
    public GioHangResponse gop(@AuthenticationPrincipal NguoiDungPrincipal principal,
                               @Valid @RequestBody GopGioHangRequest request) {
        return gioHangService.gop(principal.getId(), request.items());
    }
}

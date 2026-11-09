package com.techgear.controller;

import com.techgear.dto.DatHangRequest;
import com.techgear.dto.DatHangResponse;
import com.techgear.dto.TinhTienRequest;
import com.techgear.dto.TinhTienResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.DatHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dat-hang")
@RequiredArgsConstructor
public class DatHangController {

    private final DatHangService datHangService;

    @PostMapping("/tinh-tien")
    public TinhTienResponse tinhTien(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                     @Valid @RequestBody TinhTienRequest request) {
        return datHangService.tinhTien(principal == null ? null : principal.getId(), request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DatHangResponse datHang(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                   @Valid @RequestBody DatHangRequest request) {
        return datHangService.datHang(principal == null ? null : principal.getId(), request);
    }
}

package com.techgear.controller;

import com.techgear.dto.DanhGiaRequest;
import com.techgear.dto.DanhGiaResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.DanhGiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/danh-gia")
@RequiredArgsConstructor
public class DanhGiaKhachHangController {

    private final DanhGiaService danhGiaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DanhGiaResponse tao(@AuthenticationPrincipal NguoiDungPrincipal principal,
                               @Valid @RequestBody DanhGiaRequest request) {
        return danhGiaService.tao(principal.getId(), request);
    }
}

package com.techgear.controller;

import com.techgear.dto.ThuongHieuRequest;
import com.techgear.dto.ThuongHieuResponse;
import com.techgear.service.ThuongHieuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Quản lý thương hiệu - chỉ tài khoản quản trị (cấu hình ở SecurityConfig: /api/admin/**). */
@RestController
@RequestMapping("/api/admin/thuong-hieu")
@RequiredArgsConstructor
public class AdminThuongHieuController {

    private final ThuongHieuService thuongHieuService;

    @GetMapping
    public List<ThuongHieuResponse> layTatCa() {
        return thuongHieuService.layTatCa();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThuongHieuResponse tao(@Valid @RequestBody ThuongHieuRequest req) {
        return thuongHieuService.tao(req);
    }

    @PutMapping("/{id}")
    public ThuongHieuResponse capNhat(@PathVariable Long id, @Valid @RequestBody ThuongHieuRequest req) {
        return thuongHieuService.capNhat(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoa(@PathVariable Long id) {
        thuongHieuService.xoa(id);
    }
}

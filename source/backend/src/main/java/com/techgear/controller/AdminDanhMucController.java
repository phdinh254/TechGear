package com.techgear.controller;

import com.techgear.dto.admin.AdminDanhMucRequest;
import com.techgear.dto.admin.AdminDanhMucResponse;
import com.techgear.service.admin.AdminDanhMucService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Quản lý danh mục - chỉ tài khoản quản trị (cấu hình ở SecurityConfig: /api/admin/**). */
@Tag(name = "Quản trị - Danh mục")
@RestController
@RequestMapping("/api/admin/danh-muc")
@RequiredArgsConstructor
public class AdminDanhMucController {

    private final AdminDanhMucService adminDanhMucService;

    @GetMapping
    public List<AdminDanhMucResponse> layCay() {
        return adminDanhMucService.layCay();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDanhMucResponse tao(@Valid @RequestBody AdminDanhMucRequest req) {
        return adminDanhMucService.tao(req);
    }

    @PutMapping("/{id}")
    public AdminDanhMucResponse capNhat(@PathVariable Long id, @Valid @RequestBody AdminDanhMucRequest req) {
        return adminDanhMucService.capNhat(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoa(@PathVariable Long id) {
        adminDanhMucService.xoa(id);
    }
}

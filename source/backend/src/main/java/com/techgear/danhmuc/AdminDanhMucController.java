package com.techgear.danhmuc;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/danh-muc")
@RequiredArgsConstructor
class AdminDanhMucController {

    private final AdminDanhMucService service;

    @GetMapping
    List<AdminDanhMucService.DanhMucQT> danhSach() {
        return service.danhSach();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminDanhMucService.DanhMucQT tao(@Valid @RequestBody AdminDanhMucService.DanhMucRequest req) {
        return service.tao(req);
    }

    @PutMapping("/{id}")
    AdminDanhMucService.DanhMucQT sua(@PathVariable Long id, @Valid @RequestBody AdminDanhMucService.DanhMucRequest req) {
        return service.sua(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void xoa(@PathVariable Long id) {
        service.xoa(id);
    }
}

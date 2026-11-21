package com.techgear.thuonghieu;

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

/** Quản trị thương hiệu; xóa mềm, không xóa khi còn sản phẩm. */
@RestController
@RequestMapping("/api/admin/thuong-hieu")
@RequiredArgsConstructor
class AdminThuongHieuController {

    private final AdminThuongHieuService service;

    @GetMapping
    List<AdminThuongHieuService.ThuongHieuQT> danhSach() {
        return service.danhSach();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminThuongHieuService.ThuongHieuQT tao(@Valid @RequestBody AdminThuongHieuService.ThuongHieuRequest req) {
        return service.luu(null, req);
    }

    @PutMapping("/{id}")
    AdminThuongHieuService.ThuongHieuQT sua(@PathVariable Long id,
                                            @Valid @RequestBody AdminThuongHieuService.ThuongHieuRequest req) {
        return service.luu(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void xoa(@PathVariable Long id) {
        service.xoa(id);
    }
}

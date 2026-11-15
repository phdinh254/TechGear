package com.techgear.danhgia;

import com.techgear.chung.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/danh-gia")
@RequiredArgsConstructor
class AdminDanhGiaController {

    private final AdminDanhGiaService service;

    @GetMapping
    PageResponse<AdminDanhGiaService.DanhGiaQT> danhSach(
            @RequestParam(required = false) AdminDanhGiaService.TrangThai trangThai,
            @RequestParam(required = false) Long sanPhamId,
            @RequestParam(required = false) @Min(value = 1, message = "Số sao từ 1 đến 5")
            @Max(value = 5, message = "Số sao từ 1 đến 5") Integer soSao,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Kích thước trang từ 1 đến 50")
            @Max(value = 50, message = "Kích thước trang từ 1 đến 50") int kichThuoc) {
        return service.danhSach(trangThai, sanPhamId, soSao, trang, kichThuoc);
    }

    @PatchMapping("/{id}")
    AdminDanhGiaService.DanhGiaQT doiTrangThai(@PathVariable Long id,
                                               @Valid @RequestBody AdminDanhGiaService.TrangThaiRequest req) {
        return service.doiTrangThai(id, req.trangThai());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void xoa(@PathVariable Long id) {
        service.xoa(id);
    }
}

package com.techgear.khuyenmai;

import com.techgear.chung.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ma-giam-gia")
@RequiredArgsConstructor
class AdminMaGiamGiaController {

    private final AdminMaGiamGiaService service;

    @GetMapping
    PageResponse<AdminMaGiamGiaService.MaGiamGiaQT> danhSach(
            @RequestParam(required = false) @Size(max = 100, message = "Từ khóa tối đa 100 ký tự") String tuKhoa,
            @RequestParam(required = false) Boolean kichHoat,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Kích thước trang từ 1 đến 50")
            @Max(value = 50, message = "Kích thước trang từ 1 đến 50") int kichThuoc) {
        return service.danhSach(tuKhoa, kichHoat, trang, kichThuoc);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminMaGiamGiaService.MaGiamGiaQT tao(@Valid @RequestBody AdminMaGiamGiaService.MaGiamGiaRequest req) {
        return service.luu(null, req);
    }

    @PutMapping("/{id}")
    AdminMaGiamGiaService.MaGiamGiaQT sua(@PathVariable Long id,
                                          @Valid @RequestBody AdminMaGiamGiaService.MaGiamGiaRequest req) {
        return service.luu(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void xoa(@PathVariable Long id) {
        service.xoa(id);
    }
}

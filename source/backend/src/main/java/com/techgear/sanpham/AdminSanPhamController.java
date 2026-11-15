package com.techgear.sanpham;

import com.techgear.chung.PageResponse;
import com.techgear.chung.baomat.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
class AdminSanPhamController {

    private final AdminSanPhamService service;

    @GetMapping("/san-pham")
    PageResponse<AdminSanPhamDto.DongDanhSach> danhSach(
            @RequestParam(required = false) @Size(max = 100, message = "Từ khóa tối đa 100 ký tự") String tuKhoa,
            @RequestParam(required = false) Long danhMucId,
            @RequestParam(required = false) Long thuongHieuId,
            @RequestParam(required = false) Boolean hienThi,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Kích thước trang từ 1 đến 50")
            @Max(value = 50, message = "Kích thước trang từ 1 đến 50") int kichThuoc) {
        return service.danhSach(tuKhoa, danhMucId, thuongHieuId, hienThi, trang, kichThuoc);
    }

    @GetMapping("/san-pham/{id}")
    AdminSanPhamDto.SanPhamQT chiTiet(@PathVariable Long id) {
        return service.chiTiet(id);
    }

    @PostMapping("/san-pham")
    @ResponseStatus(HttpStatus.CREATED)
    AdminSanPhamDto.SanPhamQT tao(@Valid @RequestBody AdminSanPhamDto.SanPhamRequest req) {
        return service.tao(req);
    }

    @PutMapping("/san-pham/{id}")
    AdminSanPhamDto.SanPhamQT sua(@PathVariable Long id, @Valid @RequestBody AdminSanPhamDto.SanPhamRequest req) {
        return service.sua(id, req);
    }

    @DeleteMapping("/san-pham/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void xoa(@PathVariable Long id) {
        service.xoa(id);
    }

    @PostMapping("/san-pham/{id}/tuy-chon")
    @ResponseStatus(HttpStatus.CREATED)
    AdminSanPhamDto.SanPhamQT themTuyChon(@PathVariable Long id, @Valid @RequestBody AdminSanPhamDto.TuyChonRequest req) {
        return service.themTuyChon(id, req);
    }

    @PutMapping("/tuy-chon/{id}")
    AdminSanPhamDto.SanPhamQT suaTuyChon(@PathVariable Long id, @Valid @RequestBody AdminSanPhamDto.TuyChonRequest req) {
        return service.suaTuyChon(id, req);
    }

    @DeleteMapping("/tuy-chon/{id}")
    AdminSanPhamDto.SanPhamQT xoaTuyChon(@PathVariable Long id) {
        return service.xoaTuyChon(id);
    }

    @PostMapping("/san-pham/{id}/hinh-anh")
    @ResponseStatus(HttpStatus.CREATED)
    AdminSanPhamDto.SanPhamQT themAnh(@PathVariable Long id, @Valid @RequestBody AdminSanPhamDto.HinhAnhRequest req) {
        return service.themAnh(id, req);
    }

    @PutMapping("/hinh-anh/{id}")
    AdminSanPhamDto.SanPhamQT suaAnh(@PathVariable Long id, @Valid @RequestBody AdminSanPhamDto.HinhAnhRequest req) {
        return service.suaAnh(id, req);
    }

    @DeleteMapping("/hinh-anh/{id}")
    AdminSanPhamDto.SanPhamQT xoaAnh(@PathVariable Long id) {
        return service.xoaAnh(id);
    }

    @PostMapping("/san-pham/{id}/bien-the")
    @ResponseStatus(HttpStatus.CREATED)
    AdminSanPhamDto.SanPhamQT themBienThe(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                          @Valid @RequestBody AdminSanPhamDto.BienTheRequest req) {
        return service.themBienThe(id, req, JwtService.nguoiDungId(jwt));
    }

    @PutMapping("/bien-the/{id}")
    AdminSanPhamDto.SanPhamQT suaBienThe(@PathVariable Long id, @Valid @RequestBody AdminSanPhamDto.BienTheRequest req) {
        return service.suaBienThe(id, req);
    }

    @DeleteMapping("/bien-the/{id}")
    AdminSanPhamDto.SanPhamQT xoaBienThe(@PathVariable Long id) {
        return service.xoaBienThe(id);
    }
}

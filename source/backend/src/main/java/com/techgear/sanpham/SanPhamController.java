package com.techgear.sanpham;

import com.techgear.chung.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
class SanPhamController {

    private final SanPhamService sanPhamService;

    @GetMapping
    PageResponse<SanPhamTomTat> tim(
            @RequestParam(required = false) @Size(max = 100, message = "Từ khóa tối đa 100 ký tự") String tuKhoa,
            @RequestParam(required = false) @Size(max = 120, message = "Danh mục không hợp lệ") String danhMuc,
            @RequestParam(required = false) @Size(max = 20, message = "Chọn tối đa 20 thương hiệu") List<String> thuongHieu,
            @RequestParam(required = false) @PositiveOrZero(message = "Giá từ không được âm") Long giaTu,
            @RequestParam(required = false) @PositiveOrZero(message = "Giá đến không được âm") Long giaDen,
            @RequestParam(defaultValue = "moi_nhat") SapXep sapXep,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "12") @Min(value = 1, message = "Kích thước trang từ 1 đến 48")
            @Max(value = 48, message = "Kích thước trang từ 1 đến 48") int kichThuoc) {
        return sanPhamService.tim(tuKhoa, danhMuc, thuongHieu, giaTu, giaDen, sapXep, trang, kichThuoc);
    }

    @GetMapping("/noi-bat")
    List<SanPhamTomTat> noiBat() {
        return sanPhamService.noiBat();
    }

    @GetMapping("/{slug}")
    SanPhamChiTiet chiTiet(@PathVariable String slug) {
        return sanPhamService.chiTiet(slug);
    }
}

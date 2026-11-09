package com.techgear.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.techgear.dto.PageResponse;
import com.techgear.dto.DanhGiaSanPhamResponse;
import com.techgear.dto.SanPhamChiTietResponse;
import com.techgear.dto.SanPhamTomTatResponse;
import com.techgear.service.DanhGiaService;
import com.techgear.service.SanPhamService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Sản phẩm")
@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@Validated
public class SanPhamController {

    private final SanPhamService sanPhamService;
    private final DanhGiaService danhGiaService;

    /**
     * VD: GET /api/san-pham?tuKhoa=sac&danhMuc=sac-va-cap&thuongHieu=anker
     *         &giaTu=100000&giaDen=1000000&sapXep=gia_tang&trang=0&kichThuoc=12
     */
    @GetMapping
    public PageResponse<SanPhamTomTatResponse> timKiem(
            @Size(max = 100, message = "Từ khóa tối đa 100 ký tự")
            @RequestParam(required = false) String tuKhoa,
            @Size(max = 120, message = "Slug danh mục tối đa 120 ký tự")
            @RequestParam(required = false) String danhMuc,
            @Size(max = 120, message = "Slug thương hiệu tối đa 120 ký tự")
            @RequestParam(required = false) String thuongHieu,
            @DecimalMin(value = "0", message = "Giá từ không được âm")
            @RequestParam(required = false) BigDecimal giaTu,
            @DecimalMin(value = "0", message = "Giá đến không được âm")
            @RequestParam(required = false) BigDecimal giaDen,
            @Pattern(regexp = "^(moi_nhat|gia_tang|gia_giam|pho_bien)$",
                    message = "Kiểu sắp xếp không hợp lệ")
            @RequestParam(required = false) String sapXep,
            @Min(value = 0, message = "Trang không được âm")
            @RequestParam(defaultValue = "0") int trang,
            @Min(value = 1, message = "Kích thước trang tối thiểu là 1")
            @Max(value = 48, message = "Kích thước trang tối đa là 48")
            @RequestParam(defaultValue = "12") int kichThuoc) {
        return sanPhamService.timKiem(tuKhoa, danhMuc, thuongHieu, giaTu, giaDen, sapXep, trang, kichThuoc);
    }

    @GetMapping("/noi-bat")
    public List<SanPhamTomTatResponse> layNoiBat() {
        return sanPhamService.layNoiBat();
    }

    @GetMapping("/{slug}")
    public SanPhamChiTietResponse layChiTiet(@PathVariable String slug) {
        return sanPhamService.layChiTiet(slug);
    }

    @GetMapping("/{slug}/danh-gia")
    public DanhGiaSanPhamResponse layDanhGia(
            @PathVariable @Size(max = 120, message = "Slug sản phẩm tối đa 120 ký tự") String slug,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang không được âm") int trang,
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "Kích thước trang tối thiểu là 1")
            @Max(value = 20, message = "Kích thước trang tối đa là 20") int kichThuoc) {
        return danhGiaService.layTheoSanPham(slug, trang, kichThuoc);
    }
}

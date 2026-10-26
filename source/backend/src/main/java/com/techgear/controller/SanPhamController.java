package com.techgear.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.techgear.dto.PageResponse;
import com.techgear.dto.SanPhamChiTietResponse;
import com.techgear.dto.SanPhamTomTatResponse;
import com.techgear.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Sản phẩm")
@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;

    /**
     * VD: GET /api/san-pham?tuKhoa=sac&danhMuc=sac-va-cap&thuongHieu=anker
     *         &giaTu=100000&giaDen=1000000&sapXep=gia_tang&trang=0&kichThuoc=12
     */
    @GetMapping
    public PageResponse<SanPhamTomTatResponse> timKiem(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(required = false) String danhMuc,
            @RequestParam(required = false) String thuongHieu,
            @RequestParam(required = false) BigDecimal giaTu,
            @RequestParam(required = false) BigDecimal giaDen,
            @RequestParam(required = false) String sapXep,
            @RequestParam(defaultValue = "0") int trang,
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
}

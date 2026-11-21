package com.techgear.danhgia;

import com.techgear.chung.PageResponse;
import com.techgear.chung.baomat.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
class DanhGiaController {

    private final DanhGiaService danhGiaService;

    @PostMapping("/api/danh-gia")
    @ResponseStatus(HttpStatus.CREATED)
    DanhGiaDto.DanhGia viet(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DanhGiaDto.VietRequest req) {
        return danhGiaService.viet(JwtService.nguoiDungId(jwt), req);
    }

    @GetMapping("/api/danh-gia/co-the-viet")
    List<DanhGiaDto.CoTheViet> coTheViet(@AuthenticationPrincipal Jwt jwt) {
        return danhGiaService.coTheViet(JwtService.nguoiDungId(jwt));
    }

    @GetMapping("/api/san-pham/{slug}/danh-gia")
    PageResponse<DanhGiaDto.CongKhai> cuaSanPham(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Kích thước trang từ 1 đến 50")
            @Max(value = 50, message = "Kích thước trang từ 1 đến 50") int kichThuoc) {
        return danhGiaService.cuaSanPham(slug, trang, kichThuoc);
    }
}

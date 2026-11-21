package com.techgear.donhang;

import com.techgear.chung.PageResponse;
import com.techgear.chung.baomat.JwtService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/don-hang")
@RequiredArgsConstructor
class DonHangController {

    private final DonHangService donHangService;

    @PostMapping("/tinh-thu")
    DonHangResponse.TinhThu tinhThu(@Valid @RequestBody YeuCauDonHang.TinhThu req) {
        return donHangService.tinhThu(req);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DonHangResponse.ChiTiet datHang(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DatHangRequest req) {
        return donHangService.datHang(JwtService.nguoiDungId(jwt), req);
    }

    @PostMapping("/vang-lai")
    @ResponseStatus(HttpStatus.CREATED)
    DonHangResponse.ChiTiet datHangVangLai(@Valid @RequestBody DatHangRequest req, HttpServletRequest request) {
        return donHangService.datHangVangLai(req, request.getRemoteAddr());
    }

    @GetMapping
    PageResponse<DonHangResponse.TomTat> cuaToi(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) TrangThaiDonHang trangThai,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Kích thước trang từ 1 đến 50")
            @Max(value = 50, message = "Kích thước trang từ 1 đến 50") int kichThuoc) {
        return donHangService.cuaToi(JwtService.nguoiDungId(jwt), trangThai, trang, kichThuoc);
    }

    @GetMapping("/{maDonHang}")
    DonHangResponse.ChiTiet chiTiet(@AuthenticationPrincipal Jwt jwt, @PathVariable String maDonHang) {
        return donHangService.chiTiet(JwtService.nguoiDungId(jwt), maDonHang);
    }

    @PostMapping("/{maDonHang}/huy")
    DonHangResponse.ChiTiet huy(@AuthenticationPrincipal Jwt jwt, @PathVariable String maDonHang,
                                @Valid @RequestBody(required = false) YeuCauDonHang.Huy req) {
        return donHangService.huyCuaKhach(JwtService.nguoiDungId(jwt), maDonHang, req == null ? null : req.lyDo());
    }

    @PostMapping("/tra-cuu")
    DonHangResponse.ChiTiet traCuu(@Valid @RequestBody YeuCauDonHang.TraCuu req, HttpServletRequest request) {
        return donHangService.traCuu(req, request.getRemoteAddr());
    }
}

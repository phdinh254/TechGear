package com.techgear.controller;

import com.techgear.dto.DonHangResponse;
import com.techgear.dto.HuyDonRequest;
import com.techgear.dto.PageResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.DonHangKhachHangService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/don-hang-cua-toi")
@RequiredArgsConstructor
@Validated
public class DonHangKhachHangController {

    private final DonHangKhachHangService donHangService;

    @GetMapping
    public PageResponse<DonHangResponse> layDanhSach(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int trang,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int kichThuoc) {
        return donHangService.layDanhSach(principal.getId(), trang, kichThuoc);
    }

    @GetMapping("/{maDonHang}")
    public DonHangResponse layChiTiet(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                      @PathVariable String maDonHang) {
        return donHangService.layChiTiet(principal.getId(), maDonHang);
    }

    @PatchMapping("/{maDonHang}/huy")
    public DonHangResponse huy(@AuthenticationPrincipal NguoiDungPrincipal principal,
                               @PathVariable String maDonHang,
                               @Valid @RequestBody HuyDonRequest request) {
        return donHangService.huy(principal.getId(), maDonHang, request.lyDo());
    }
}

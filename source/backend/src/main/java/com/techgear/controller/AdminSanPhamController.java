package com.techgear.controller;

import com.techgear.dto.PageResponse;
import com.techgear.dto.admin.AdminSanPhamChiTietResponse;
import com.techgear.dto.admin.AdminSanPhamRequest;
import com.techgear.dto.admin.AdminSanPhamTomTatResponse;
import com.techgear.dto.admin.HienThiRequest;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.admin.AdminSanPhamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Quản lý sản phẩm - chỉ tài khoản quản trị (cấu hình ở SecurityConfig: /api/admin/**). */
@Tag(name = "Quản trị - Sản phẩm")
@Validated
@RestController
@RequestMapping("/api/admin/san-pham")
@RequiredArgsConstructor
public class AdminSanPhamController {

    private final AdminSanPhamService adminSanPhamService;

    /** trangThai: dang_ban | dang_an | het_hang. */
    @GetMapping
    public PageResponse<AdminSanPhamTomTatResponse> timKiem(@RequestParam(required = false) String tuKhoa,
                                                            @RequestParam(required = false) Long danhMucId,
                                                            @RequestParam(required = false) Long thuongHieuId,
                                                            @RequestParam(required = false) String trangThai,
                                                            @RequestParam(defaultValue = "0") int trang,
                                                            @RequestParam(defaultValue = "20") int kichThuoc) {
        return adminSanPhamService.timKiem(tuKhoa, danhMucId, thuongHieuId, trangThai, trang, kichThuoc);
    }

    @GetMapping("/{id}")
    public AdminSanPhamChiTietResponse layChiTiet(@PathVariable Long id) {
        return adminSanPhamService.layChiTiet(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminSanPhamChiTietResponse tao(@Valid @RequestBody AdminSanPhamRequest req,
                                           @AuthenticationPrincipal NguoiDungPrincipal principal) {
        return adminSanPhamService.tao(req, principal.getId());
    }

    @PutMapping("/{id}")
    public AdminSanPhamChiTietResponse capNhat(@PathVariable Long id, @Valid @RequestBody AdminSanPhamRequest req,
                                               @AuthenticationPrincipal NguoiDungPrincipal principal) {
        return adminSanPhamService.capNhat(id, req, principal.getId());
    }

    @PatchMapping("/{id}/hien-thi")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void doiHienThi(@PathVariable Long id, @Valid @RequestBody HienThiRequest req) {
        adminSanPhamService.doiHienThi(id, req.hienThi());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoa(@PathVariable Long id) {
        adminSanPhamService.xoa(id);
    }
}

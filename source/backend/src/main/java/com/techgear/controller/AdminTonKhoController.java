package com.techgear.controller;

import com.techgear.dto.PageResponse;
import com.techgear.dto.admin.AdminTonKhoResponse;
import com.techgear.dto.admin.DieuChinhKhoRequest;
import com.techgear.dto.admin.LichSuTonKhoResponse;
import com.techgear.dto.admin.NhapKhoRequest;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.admin.AdminTonKhoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Quản lý tồn kho - chỉ tài khoản quản trị (cấu hình ở SecurityConfig: /api/admin/**). */
@Tag(name = "Quản trị - Tồn kho")
@Validated
@RestController
@RequestMapping("/api/admin/ton-kho")
@RequiredArgsConstructor
public class AdminTonKhoController {

    private final AdminTonKhoService adminTonKhoService;

    /** trangThai: con_hang | sap_het | het_hang. */
    @GetMapping
    public PageResponse<AdminTonKhoResponse> danhSach(@RequestParam(required = false) String tuKhoa,
                                                      @RequestParam(required = false) String trangThai,
                                                      @RequestParam(defaultValue = "0") int trang,
                                                      @RequestParam(defaultValue = "20") int kichThuoc) {
        return adminTonKhoService.danhSach(tuKhoa, trangThai, trang, kichThuoc);
    }

    @PostMapping("/{bienTheId}/nhap-kho")
    public AdminTonKhoResponse nhapKho(@PathVariable Long bienTheId, @Valid @RequestBody NhapKhoRequest req,
                                       @AuthenticationPrincipal NguoiDungPrincipal principal) {
        return adminTonKhoService.nhapKho(bienTheId, req, principal.getId());
    }

    @PostMapping("/{bienTheId}/dieu-chinh")
    public AdminTonKhoResponse dieuChinh(@PathVariable Long bienTheId, @Valid @RequestBody DieuChinhKhoRequest req,
                                         @AuthenticationPrincipal NguoiDungPrincipal principal) {
        return adminTonKhoService.dieuChinh(bienTheId, req, principal.getId());
    }

    @GetMapping("/{bienTheId}/lich-su")
    public PageResponse<LichSuTonKhoResponse> lichSu(@PathVariable Long bienTheId,
                                                     @RequestParam(defaultValue = "0") int trang,
                                                     @RequestParam(defaultValue = "20") int kichThuoc) {
        return adminTonKhoService.lichSu(bienTheId, trang, kichThuoc);
    }
}

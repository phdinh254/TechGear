package com.techgear.taikhoan;

import com.techgear.chung.PageResponse;
import com.techgear.chung.baomat.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/nguoi-dung")
@RequiredArgsConstructor
class AdminNguoiDungController {

    private final AdminNguoiDungService service;

    @GetMapping
    PageResponse<AdminNguoiDungService.NguoiDungQT> danhSach(
            @RequestParam(required = false) @Size(max = 150, message = "Từ khóa tối đa 150 ký tự") String tuKhoa,
            @RequestParam(required = false) NguoiDung.VaiTro vaiTro,
            @RequestParam(required = false) NguoiDung.TrangThai trangThai,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Kích thước trang từ 1 đến 50")
            @Max(value = 50, message = "Kích thước trang từ 1 đến 50") int kichThuoc) {
        return service.danhSach(tuKhoa, vaiTro, trangThai, trang, kichThuoc);
    }

    @PatchMapping("/{id}/trang-thai")
    AdminNguoiDungService.NguoiDungQT doiTrangThai(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                   @Valid @RequestBody AdminNguoiDungService.TrangThaiRequest req) {
        return service.doiTrangThai(id, req.trangThai(), JwtService.nguoiDungId(jwt));
    }
}

package com.techgear.sanpham;

import com.techgear.chung.PageResponse;
import com.techgear.chung.baomat.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
class AdminKhoController {

    private final AdminKhoService service;

    @PostMapping("/bien-the/{id}/nhap-kho")
    AdminKhoService.TonKho nhapKho(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                   @Valid @RequestBody AdminKhoService.NhapKhoRequest req) {
        return service.nhapKho(id, req, JwtService.nguoiDungId(jwt));
    }

    @PostMapping("/bien-the/{id}/dieu-chinh")
    AdminKhoService.TonKho dieuChinh(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                     @Valid @RequestBody AdminKhoService.DieuChinhRequest req) {
        return service.dieuChinh(id, req, JwtService.nguoiDungId(jwt));
    }

    @GetMapping("/kho/lich-su")
    PageResponse<AdminKhoService.LichSu> lichSu(
            @RequestParam(required = false) Long bienTheId,
            @RequestParam(required = false) AdminKhoService.LoaiKho loai,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Kích thước trang từ 1 đến 100")
            @Max(value = 100, message = "Kích thước trang từ 1 đến 100") int kichThuoc) {
        return service.lichSu(bienTheId, loai, trang, kichThuoc);
    }

    @GetMapping("/kho/sap-het")
    List<AdminKhoService.SapHet> sapHet() {
        return service.sapHet();
    }
}

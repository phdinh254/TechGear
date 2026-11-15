package com.techgear.donhang;

import com.techgear.chung.PageResponse;
import com.techgear.chung.baomat.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/don-hang")
@RequiredArgsConstructor
class AdminDonHangController {

    private final AdminDonHangService service;

    @GetMapping
    PageResponse<AdminDonHangService.DongDanhSach> danhSach(
            @RequestParam(required = false) @Size(max = 150, message = "Từ khóa tối đa 150 ký tự") String tuKhoa,
            @RequestParam(required = false) TrangThaiDonHang trangThai,
            @RequestParam(required = false) AdminDonHangService.TrangThaiThanhToan trangThaiThanhToan,
            @RequestParam(required = false) DatHangRequest.PhuongThucThanhToan phuongThucThanhToan,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Trang phải từ 0 trở lên") int trang,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Kích thước trang từ 1 đến 50")
            @Max(value = 50, message = "Kích thước trang từ 1 đến 50") int kichThuoc) {
        return service.danhSach(tuKhoa, trangThai, trangThaiThanhToan, phuongThucThanhToan, tuNgay, denNgay, trang,
                kichThuoc);
    }

    @GetMapping("/{maDonHang}")
    AdminDonHangService.ChiTietQT chiTiet(@PathVariable String maDonHang) {
        return service.chiTiet(maDonHang);
    }

    @PatchMapping("/{maDonHang}/trang-thai")
    AdminDonHangService.ChiTietQT doiTrangThai(@AuthenticationPrincipal Jwt jwt, @PathVariable String maDonHang,
                                               @Valid @RequestBody AdminDonHangService.DoiTrangThaiRequest req) {
        return service.doiTrangThai(maDonHang, req, JwtService.nguoiDungId(jwt));
    }

    @PatchMapping("/{maDonHang}/thanh-toan")
    AdminDonHangService.ChiTietQT doiThanhToan(@PathVariable String maDonHang,
                                               @Valid @RequestBody AdminDonHangService.ThanhToanRequest req) {
        return service.doiThanhToan(maDonHang, req);
    }

    @PatchMapping("/{maDonHang}/ghi-chu")
    AdminDonHangService.ChiTietQT ghiChu(@PathVariable String maDonHang,
                                         @Valid @RequestBody AdminDonHangService.GhiChuRequest req) {
        return service.ghiChu(maDonHang, req);
    }
}

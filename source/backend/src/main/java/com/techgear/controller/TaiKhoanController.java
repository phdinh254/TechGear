package com.techgear.controller;

import com.techgear.dto.CapNhatHoSoRequest;
import com.techgear.dto.DiaChiRequest;
import com.techgear.dto.DiaChiResponse;
import com.techgear.dto.DoiMatKhauRequest;
import com.techgear.dto.NguoiDungResponse;
import com.techgear.dto.ThongBaoResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.TaiKhoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tai-khoan")
@RequiredArgsConstructor
public class TaiKhoanController {

    private final TaiKhoanService taiKhoanService;

    @PutMapping("/ho-so")
    public NguoiDungResponse capNhatHoSo(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                         @Valid @RequestBody CapNhatHoSoRequest request) {
        return taiKhoanService.capNhatHoSo(principal.getId(), request);
    }

    @PutMapping("/mat-khau")
    public ThongBaoResponse doiMatKhau(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                       @Valid @RequestBody DoiMatKhauRequest request) {
        taiKhoanService.doiMatKhau(principal.getId(), request);
        return new ThongBaoResponse("Đổi mật khẩu thành công");
    }

    @GetMapping("/dia-chi")
    public List<DiaChiResponse> layDiaChi(@AuthenticationPrincipal NguoiDungPrincipal principal) {
        return taiKhoanService.layDiaChi(principal.getId());
    }

    @PostMapping("/dia-chi")
    @ResponseStatus(HttpStatus.CREATED)
    public DiaChiResponse taoDiaChi(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                    @Valid @RequestBody DiaChiRequest request) {
        return taiKhoanService.taoDiaChi(principal.getId(), request);
    }

    @PutMapping("/dia-chi/{id}")
    public DiaChiResponse capNhatDiaChi(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                        @PathVariable Long id,
                                        @Valid @RequestBody DiaChiRequest request) {
        return taiKhoanService.capNhatDiaChi(principal.getId(), id, request);
    }

    @PatchMapping("/dia-chi/{id}/mac-dinh")
    public DiaChiResponse datMacDinh(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                     @PathVariable Long id) {
        return taiKhoanService.datMacDinh(principal.getId(), id);
    }

    @DeleteMapping("/dia-chi/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoaDiaChi(@AuthenticationPrincipal NguoiDungPrincipal principal, @PathVariable Long id) {
        taiKhoanService.xoaDiaChi(principal.getId(), id);
    }
}

package com.techgear.controller;

import com.techgear.dto.SanPhamTomTatResponse;
import com.techgear.dto.YeuThichStatusResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.YeuThichService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yeu-thich")
@RequiredArgsConstructor
public class YeuThichController {

    private final YeuThichService yeuThichService;

    @GetMapping
    public List<SanPhamTomTatResponse> layDanhSach(@AuthenticationPrincipal NguoiDungPrincipal principal) {
        return yeuThichService.layDanhSach(principal.getId());
    }

    @GetMapping("/{sanPhamId}")
    public YeuThichStatusResponse kiemTra(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                          @PathVariable Long sanPhamId) {
        return new YeuThichStatusResponse(yeuThichService.kiemTra(principal.getId(), sanPhamId));
    }

    @PostMapping("/{sanPhamId}")
    @ResponseStatus(HttpStatus.CREATED)
    public YeuThichStatusResponse them(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                        @PathVariable Long sanPhamId) {
        yeuThichService.them(principal.getId(), sanPhamId);
        return new YeuThichStatusResponse(true);
    }

    @DeleteMapping("/{sanPhamId}")
    public YeuThichStatusResponse xoa(@AuthenticationPrincipal NguoiDungPrincipal principal,
                                      @PathVariable Long sanPhamId) {
        yeuThichService.xoa(principal.getId(), sanPhamId);
        return new YeuThichStatusResponse(false);
    }
}

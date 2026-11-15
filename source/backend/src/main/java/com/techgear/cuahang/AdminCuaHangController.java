package com.techgear.cuahang;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
class AdminCuaHangController {

    private final AdminCuaHangService service;

    @GetMapping("/banner")
    List<AdminCuaHangService.BannerQT> danhSachBanner() {
        return service.danhSachBanner();
    }

    @PostMapping("/banner")
    @ResponseStatus(HttpStatus.CREATED)
    AdminCuaHangService.BannerQT taoBanner(@Valid @RequestBody AdminCuaHangService.BannerRequest req) {
        return service.luuBanner(null, req);
    }

    @PutMapping("/banner/{id}")
    AdminCuaHangService.BannerQT suaBanner(@PathVariable Long id, @Valid @RequestBody AdminCuaHangService.BannerRequest req) {
        return service.luuBanner(id, req);
    }

    @DeleteMapping("/banner/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void xoaBanner(@PathVariable Long id) {
        service.xoaBanner(id);
    }

    @GetMapping("/cai-dat")
    List<AdminCuaHangService.CaiDat> caiDat() {
        return service.caiDat();
    }

    @PutMapping("/cai-dat")
    List<AdminCuaHangService.CaiDat> suaCaiDat(@RequestBody Map<String, String> giaTris) {
        return service.suaCaiDat(giaTris);
    }
}

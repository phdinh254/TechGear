package com.techgear.controller;

import com.techgear.dto.BannerResponse;
import com.techgear.service.CuaHangService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Cửa hàng")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CuaHangController {

    private final CuaHangService cuaHangService;

    @GetMapping("/banner")
    public List<BannerResponse> layBanner() {
        return cuaHangService.layBannerDangHienThi();
    }

    @GetMapping("/cai-dat")
    public Map<String, String> layCaiDat() {
        return cuaHangService.layCaiDatCongKhai();
    }
}

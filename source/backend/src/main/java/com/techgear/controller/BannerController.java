package com.techgear.controller;

import com.techgear.dto.BannerResponse;
import com.techgear.service.BannerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Banner")
@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public List<BannerResponse> layDangHien() {
        return bannerService.layDangHien();
    }
}

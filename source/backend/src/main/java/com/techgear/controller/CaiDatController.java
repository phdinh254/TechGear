package com.techgear.controller;

import com.techgear.service.CaiDatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Cài đặt cửa hàng")
@RestController
@RequestMapping("/api/cai-dat")
@RequiredArgsConstructor
public class CaiDatController {

    private final CaiDatService caiDatService;

    @GetMapping
    public Map<String, String> layTatCa() {
        return caiDatService.layTatCa();
    }
}

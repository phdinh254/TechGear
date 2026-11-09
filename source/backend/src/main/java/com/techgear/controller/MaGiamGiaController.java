package com.techgear.controller;

import com.techgear.dto.MaGiamGiaResponse;
import com.techgear.service.MaGiamGiaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Mã khuyến mãi")
@RestController
@RequestMapping("/api/ma-giam-gia")
@RequiredArgsConstructor
public class MaGiamGiaController {

    private final MaGiamGiaService maGiamGiaService;

    @GetMapping
    public List<MaGiamGiaResponse> layDangApDung() {
        return maGiamGiaService.layMaDangApDung();
    }
}

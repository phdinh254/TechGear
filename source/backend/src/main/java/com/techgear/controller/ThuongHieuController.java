package com.techgear.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.techgear.dto.ThuongHieuResponse;
import com.techgear.service.ThuongHieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Thương hiệu")
@RestController
@RequestMapping("/api/thuong-hieu")
@RequiredArgsConstructor
public class ThuongHieuController {

    private final ThuongHieuService thuongHieuService;

    @GetMapping
    public List<ThuongHieuResponse> layDanhSach() {
        return thuongHieuService.layDangHienThi();
    }
}

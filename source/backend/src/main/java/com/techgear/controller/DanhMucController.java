package com.techgear.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.techgear.dto.DanhMucResponse;
import com.techgear.service.DanhMucService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Danh mục")
@RestController
@RequestMapping("/api/danh-muc")
@RequiredArgsConstructor
public class DanhMucController {

    private final DanhMucService danhMucService;

    @GetMapping
    public List<DanhMucResponse> layCayDanhMuc() {
        return danhMucService.layCayDanhMuc();
    }
}

package com.techgear.danhmuc;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
class DanhMucController {

    private final DanhMucService danhMucService;

    @GetMapping("/api/danh-muc")
    List<DanhMucResponse> cay() {
        return danhMucService.cay();
    }
}

package com.techgear.controller;

import com.techgear.dto.DonHangResponse;
import com.techgear.dto.TraCuuDonHangRequest;
import com.techgear.service.DonHangKhachHangService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Dùng POST để email / số điện thoại không nằm trên URL (tránh lộ qua log, lịch sử trình duyệt).
 */
@Tag(name = "Đơn hàng")
@RestController
@RequestMapping("/api/tra-cuu-don-hang")
@RequiredArgsConstructor
public class TraCuuDonHangController {

    private final DonHangKhachHangService donHangService;

    @PostMapping
    public DonHangResponse traCuu(@Valid @RequestBody TraCuuDonHangRequest request) {
        return donHangService.traCuu(request.maDonHang(), request.lienHe());
    }
}

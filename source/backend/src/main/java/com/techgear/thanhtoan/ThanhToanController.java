package com.techgear.thanhtoan;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/thanh-toan/payos")
@RequiredArgsConstructor
class ThanhToanController {

    private final ThanhToanService thanhToanService;

    record DaNhan(boolean success) {
    }

    @PostMapping("/{maDonHang}")
    ThanhToanService.LinkThanhToan taoLink(@PathVariable String maDonHang) {
        return thanhToanService.taoLink(maDonHang);
    }

    /** PayOS gọi; trả 200 khi đã xử lý hoặc bỏ qua dữ liệu không khớp. Lỗi bất ngờ trả 500 để PayOS gửi lại. */
    @PostMapping("/webhook")
    DaNhan webhook(@RequestBody Map<String, Object> body) {
        thanhToanService.xuLyWebhook(body);
        return new DaNhan(true);
    }

    @GetMapping("/ket-qua")
    ThanhToanService.KetQua ketQua(@RequestParam(required = false) String orderCode,
                                   @RequestParam(required = false) String ma) {
        return thanhToanService.ketQua(orderCode, ma);
    }
}

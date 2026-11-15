package com.techgear.thanhtoan;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/thanh-toan/vnpay")
@RequiredArgsConstructor
class ThanhToanController {

    private final ThanhToanService thanhToanService;

    record DuongDan(String duongDan) {
    }

    @PostMapping("/{maDonHang}")
    DuongDan taoUrl(@PathVariable String maDonHang, HttpServletRequest request) {
        return new DuongDan(thanhToanService.taoUrl(maDonHang, request.getRemoteAddr()));
    }

    /** VNPay gọi; luôn trả 200 kèm RspCode (lỗi bất ngờ, kể cả lúc commit, là 99 để VNPay gọi lại). */
    @GetMapping("/ipn")
    Map<String, String> ipn(@RequestParam Map<String, String> thamSo) {
        try {
            return thanhToanService.xuLyIpn(thamSo);
        } catch (RuntimeException e) {
            log.error("Lỗi xử lý IPN VNPay, mã tham chiếu {}", thamSo.get("vnp_TxnRef"), e);
            return ThanhToanService.phanHoi("99", "Unknow error");
        }
    }

    @GetMapping("/ket-qua")
    ThanhToanService.KetQua ketQua(@RequestParam Map<String, String> thamSo) {
        return thanhToanService.ketQua(thamSo);
    }
}

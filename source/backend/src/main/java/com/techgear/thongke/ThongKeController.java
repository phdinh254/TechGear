package com.techgear.thongke;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/thong-ke")
@RequiredArgsConstructor
class ThongKeController {

    private final ThongKeService service;

    @GetMapping("/doanh-thu")
    List<ThongKeService.Moc> doanhThu(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tu,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate den,
                                      @RequestParam(defaultValue = "ngay") ThongKeService.Theo theo) {
        return service.doanhThu(tu, den, theo);
    }

    @GetMapping("/don-hang")
    ThongKeService.DonHang donHang(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tu,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate den) {
        return service.donHang(tu, den);
    }

    @GetMapping("/ban-chay")
    List<ThongKeService.BanChay> banChay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tu,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate den,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Giới hạn từ 1 đến 50")
            @Max(value = 50, message = "Giới hạn từ 1 đến 50") int gioiHan) {
        return service.banChay(tu, den, gioiHan);
    }
}

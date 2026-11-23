package com.techgear.donhang;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Mỗi phút hủy các đơn PayOS quá hạn thanh toán. */
@Slf4j
@Component
@RequiredArgsConstructor
class HuyDonQuaHan {

    private final DonHangService donHangService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    void chay() {
        int soDon = donHangService.huyDonPayOsQuaHan();
        if (soDon > 0) {
            log.info("Đã tự hủy {} đơn PayOS quá hạn thanh toán", soDon);
        }
    }
}

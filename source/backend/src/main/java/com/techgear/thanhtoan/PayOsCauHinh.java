package com.techgear.thanhtoan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Slf4j
@Configuration
class PayOsCauHinh {

    @Bean
    PayOsClient payOsClient(PayOsProperties payOs, JdbcClient jdbc) {
        if (payOs.dungGiaLap()) {
            log.warn("PayOS đang GIẢ LẬP (chưa đặt khóa thật) – mọi giao dịch đều tự báo đã thanh toán");
            return new PayOsClientGiaLap(jdbc);
        }
        return new PayOsClientHttp(payOs);
    }
}

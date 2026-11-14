package com.techgear.chung;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CauHinhChung {

    /** Đồng hồ dùng chung (thay được trong test) theo giờ Việt Nam. */
    @Bean
    Clock clock() {
        return Clock.system(ThoiGian.VIET_NAM);
    }
}

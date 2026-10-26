package com.techgear.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Bật xử lý bất đồng bộ (gửi email không làm chậm phản hồi API). */
@Configuration
@EnableAsync
public class AsyncConfig {
}

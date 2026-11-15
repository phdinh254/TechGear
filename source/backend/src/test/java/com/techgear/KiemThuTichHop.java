package com.techgear;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Lớp cha cho kiểm thử tích hợp (*IT, chạy bằng mvn verify, cần Docker):
 * một PostgreSQL 17 dùng chung, lược đồ do Flyway thật tạo; mỗi test chạy trong giao dịch và được rollback.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "app.jwt.khoa-bi-mat=khoa-kiem-thu-tich-hop-du-dai-32-ky-tu-0123456789",
        "app.upload.thu-muc=target/uploads-it",
        "app.vnpay.tmn-code=TESTCODE",
        "app.vnpay.hash-secret=khoa-kiem-thu-vnpay"
})
public abstract class KiemThuTichHop {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
            .withCommand("postgres", "-c", "timezone=Asia/Ho_Chi_Minh");

    static {
        POSTGRES.start();
    }
}

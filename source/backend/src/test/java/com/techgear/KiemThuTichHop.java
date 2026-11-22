package com.techgear;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Lớp cha cho kiểm thử tích hợp (*IT, chạy bằng mvn verify, cần Docker):
 * một MySQL 8.4 dùng chung, lược đồ do Flyway thật tạo; mỗi test chạy trong giao dịch và được rollback.
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
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withCommand("--default-time-zone=+07:00", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_0900_ai_ci");

    static {
        MYSQL.start();
    }
}

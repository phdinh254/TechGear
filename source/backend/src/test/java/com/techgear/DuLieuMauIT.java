package com.techgear;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.mysql.MySQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Dữ liệu mẫu profile dev (db/du-lieu-mau) nạp được trên CSDL trống. Container riêng vì các IT khác cần CSDL trống.
 * Khởi động được nghĩa là 5 bước tự kiểm cuối R__du_lieu_mau_van_hanh.sql (SIGNAL) đã qua.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.jwt.khoa-bi-mat=khoa-kiem-thu-tich-hop-du-dai-32-ky-tu-0123456789",
        "app.upload.thu-muc=target/uploads-it",
        "spring.flyway.locations=classpath:db/migration,classpath:db/du-lieu-mau"
})
class DuLieuMauIT {

    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withCommand("--default-time-zone=+07:00", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_0900_ai_ci");

    static {
        MYSQL.start();
    }

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void napDuDuLieuMau() {
        assertEquals(35, dem("SELECT count(*) FROM san_pham"));
        assertEquals(0, dem("SELECT count(*) FROM san_pham sp WHERE NOT EXISTS "
                + "(SELECT 1 FROM hinh_anh_san_pham ha WHERE ha.san_pham_id = sp.id AND ha.la_anh_chinh)"));
        assertEquals(43, dem("SELECT count(*) FROM don_hang"));
        assertEquals(5, dem("SELECT count(DISTINCT trang_thai) FROM don_hang"));
        assertEquals(27, dem("SELECT count(*) FROM danh_gia"));
        assertEquals(1, dem("SELECT count(*) FROM nguoi_dung WHERE trang_thai = 'bi_khoa'"));
    }

    private long dem(String sql) {
        return jdbc.queryForObject(sql, Long.class);
    }
}

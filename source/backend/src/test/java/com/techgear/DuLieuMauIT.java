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
        // Thanh toán trực tuyến là PayOS: orderCode = id đơn × 100 + lần thử, dữ liệu phản hồi dạng webhook PayOS
        assertEquals(19, dem("SELECT count(*) FROM don_hang WHERE phuong_thuc_thanh_toan = 'payos'"));
        assertEquals(0, dem("SELECT count(*) FROM don_hang WHERE phuong_thuc_thanh_toan NOT IN ('cod', 'payos')"));
        assertEquals(0, dem("SELECT count(*) FROM thanh_toan tt JOIN don_hang dh ON dh.id = tt.don_hang_id "
                + "WHERE tt.phuong_thuc <> 'payos' OR tt.ma_tham_chieu NOT REGEXP '^[0-9]+$' OR tt.ma_tham_chieu DIV 100 <> dh.id "
                + "OR JSON_EXTRACT(tt.du_lieu_phan_hoi, '$.data.orderCode') <> CAST(tt.ma_tham_chieu AS UNSIGNED)"));
    }

    private long dem(String sql) {
        return jdbc.queryForObject(sql, Long.class);
    }
}

package com.techgear;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Khởi động toàn bộ ứng dụng trên MySQL thật: Flyway chạy migration, ddl-auto=validate qua. */
class KhoiDongIT extends KiemThuTichHop {

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MockMvc mvc;

    @Test
    void flywayDaChayVaCollationKhongDau() {
        // Bảng tạo bởi Flyway dùng utf8mb4_0900_ai_ci: so sánh bỏ qua hoa thường và dấu
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM cai_dat WHERE mo_ta LIKE '%PHI VAN CHUYEN%' AND khoa = 'phi_van_chuyen'", Integer.class));
        assertEquals("+07:00", jdbc.queryForObject("SELECT @@session.time_zone", String.class));
    }

    @Test
    void swaggerCongKhaiConApiCanDangNhap() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/api/khong-ton-tai"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.thongBao").value("Vui lòng đăng nhập"));
    }

    @Test
    void anhKhongTonTaiTra404Json() throws Exception {
        mvc.perform(get("/uploads/san-pham/khong-co.png"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.trangThai").value(404));
    }
}

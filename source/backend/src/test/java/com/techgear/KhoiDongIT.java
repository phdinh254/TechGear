package com.techgear;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Khởi động toàn bộ ứng dụng trên PostgreSQL thật: Flyway chạy migration, ddl-auto=validate qua. */
class KhoiDongIT extends KiemThuTichHop {

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MockMvc mvc;

    @Test
    void flywayDaChayVaUnaccentHoatDong() {
        assertEquals("Sac du phong", jdbc.queryForObject("SELECT unaccent('Sạc dự phòng')", String.class));
        assertEquals("Asia/Ho_Chi_Minh", jdbc.queryForObject("SHOW timezone", String.class));
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

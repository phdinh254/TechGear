package com.techgear;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Lớp cha cho kiểm thử API quản trị: một quản trị viên và một khách hàng mới mỗi test. */
public abstract class KiemThuQuanTri extends KiemThuTichHop {

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected JdbcTemplate jdbc;
    @Autowired
    protected EntityManager em;
    protected TaoDuLieuSanPham tao;
    protected String admin;
    protected String khach;
    protected long adminId;

    @BeforeEach
    void chuanBiQuanTri() throws Exception {
        tao = new TaoDuLieuSanPham(jdbc);
        khach = dangKy("kh-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn");
        String email = "qt-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn";
        dangKy(email);
        // test và MockMvc chung một giao dịch: đẩy rồi bỏ bộ nhớ đệm Hibernate để đăng nhập đọc vai trò mới
        em.flush();
        jdbc.update("UPDATE nguoi_dung SET vai_tro = 'quan_tri' WHERE email = ?", email);
        em.clear();
        adminId = jdbc.queryForObject("SELECT id FROM nguoi_dung WHERE email = ?", Long.class, email);
        String json = mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"matKhau\":\"matkhau123\"}".formatted(email)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        admin = "Bearer " + JsonPath.read(json, "$.token");
    }

    protected String dangKy(String email) throws Exception {
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hoTen\":\"Người dùng thử\",\"email\":\"%s\",\"matKhau\":\"matkhau123\"}".formatted(email)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    /** Gửi yêu cầu bằng token quản trị, thân JSON (null = không có thân). */
    protected ResultActions qt(MockHttpServletRequestBuilder b, String json) throws Exception {
        b.header("Authorization", admin);
        if (json != null) {
            b.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mvc.perform(b);
    }

    protected static long id(ResultActions kq) throws Exception {
        return ((Number) JsonPath.read(kq.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    protected static <T> T doc(ResultActions kq, String duongDan) throws Exception {
        return JsonPath.read(kq.andReturn().getResponse().getContentAsString(), duongDan);
    }
}

package com.techgear.taikhoan;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuTichHop;
import com.techgear.chung.baomat.JwtService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Tiện ích chung cho kiểm thử tích hợp module tài khoản. */
abstract class KiemThuTaiKhoan extends KiemThuTichHop {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    EntityManager em;
    @Autowired
    JwtEncoder jwtEncoder;

    static String emailMoi() {
        return "kh-" + UUID.randomUUID().toString().substring(0, 8) + "@vi-du.vn";
    }

    /** Đăng ký khách hàng mật khẩu "matkhau123", trả token. */
    String dangKy(String email) throws Exception {
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Nguyễn Văn An","email":"%s","matKhau":"matkhau123","soDienThoai":"0912345678"}
                        """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    Long idCua(String email) {
        return jdbc.queryForObject("SELECT id FROM nguoi_dung WHERE email = ?", Long.class, email);
    }

    /** Token hợp lệ chữ ký nhưng phát hành từ 1 phút trước (như token người dùng đang giữ từ trước). */
    String tokenCu(Long nguoiDungId) {
        Instant luc = Instant.now().minus(1, ChronoUnit.MINUTES);
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().issuer(JwtService.NHA_PHAT_HANH).subject(nguoiDungId.toString())
                        .issuedAt(luc).expiresAt(luc.plus(1, ChronoUnit.DAYS))
                        .claim(JwtService.CLAIM_VAI_TRO, "khach_hang").build())).getTokenValue();
    }

    /** Sửa CSDL trực tiếp rồi bỏ bộ nhớ đệm của Hibernate để lần đọc sau thấy dữ liệu mới. */
    void suaCsdl(String sql, Object... thamSo) {
        em.flush();
        jdbc.update(sql, thamSo);
        em.clear();
    }

    static String bearer(String token) {
        return "Bearer " + token;
    }
}

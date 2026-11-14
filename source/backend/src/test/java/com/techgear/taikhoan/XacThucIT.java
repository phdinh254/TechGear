package com.techgear.taikhoan;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuTichHop;
import com.techgear.chung.baomat.JwtService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class XacThucIT extends KiemThuTichHop {

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

    String dangKy(String email) throws Exception {
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Nguyễn Văn An","email":"%s","matKhau":"matkhau123","soDienThoai":"0912345678"}
                        """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    /** Sửa CSDL trực tiếp rồi bỏ bộ nhớ đệm của Hibernate để lần đọc sau thấy dữ liệu mới. */
    void suaCsdl(String sql, Object... thamSo) {
        em.flush();
        jdbc.update(sql, thamSo);
        em.clear();
    }

    @Test
    void dangKyTraTokenVaThongTinDungHopDongFrontend() throws Exception {
        String email = emailMoi();
        // frontend gửi kèm nhapLai và soDienThoai rỗng khi không nhập
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Trần Thị Bình","email":"%s","matKhau":"matkhau123","nhapLai":"matkhau123","soDienThoai":""}
                        """.formatted(email.toUpperCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.nguoiDung.id").isNumber())
                .andExpect(jsonPath("$.nguoiDung.hoTen").value("Trần Thị Bình"))
                .andExpect(jsonPath("$.nguoiDung.email").value(email))
                .andExpect(jsonPath("$.nguoiDung.soDienThoai").doesNotExist())
                .andExpect(jsonPath("$.nguoiDung.vaiTro").value("khach_hang"))
                .andExpect(jsonPath("$.nguoiDung.matKhau").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/api/auth/toi").header("Authorization", "Bearer " + JsonPath.read(json, "$.token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.vaiTro").value("khach_hang"));
    }

    @Test
    void dangKyEmailTrungKhacHoaThuongLa409() throws Exception {
        String email = emailMoi();
        dangKy(email);
        mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Người khác","email":"%s","matKhau":"matkhau123"}
                        """.formatted(email.toUpperCase())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Email đã được sử dụng"));
    }

    @Test
    void dangKyDuLieuSaiLiet400TheoTruong() throws Exception {
        mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"","email":"khong-phai-email","matKhau":"chiCoChu","soDienThoai":"12345"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.hoTen").exists())
                .andExpect(jsonPath("$.loi.email").value("Email không đúng định dạng"))
                .andExpect(jsonPath("$.loi.matKhau").value("Mật khẩu phải từ 8 đến 72 ký tự, có cả chữ và số"))
                .andExpect(jsonPath("$.loi.soDienThoai").value("Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"));
    }

    @Test
    void dangNhapEmailKhongPhanBietHoaThuong() throws Exception {
        String email = emailMoi();
        dangKy(email);
        mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"  %s ","matKhau":"matkhau123"}
                        """.formatted(email.toUpperCase())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.nguoiDung.email").value(email))
                .andExpect(jsonPath("$.nguoiDung.soDienThoai").value("0912345678"));
    }

    @Test
    void saiMatKhauVaEmailKhongTonTaiCungMotThongBao401() throws Exception {
        String email = emailMoi();
        dangKy(email);
        for (String e : new String[]{email, emailMoi()}) {
            mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON).content("""
                            {"email":"%s","matKhau":"saimatkhau1"}
                            """.formatted(e)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.thongBao").value("Email hoặc mật khẩu không đúng"));
        }
    }

    @Test
    void sai5LanThiLan6Bi429DuMatKhauDung() throws Exception {
        String email = emailMoi();
        dangKy(email);
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON).content("""
                    {"email":"%s","matKhau":"saimatkhau1"}
                    """.formatted(email))).andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"%s","matKhau":"matkhau123"}
                        """.formatted(email)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void taiKhoanBiKhoaKhongDangNhapDuocVaTokenCuBiTuChoi() throws Exception {
        String email = emailMoi();
        String token = dangKy(email);
        suaCsdl("UPDATE nguoi_dung SET trang_thai = 'bi_khoa' WHERE email = ?", email);

        mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"%s","matKhau":"matkhau123"}
                        """.formatted(email)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.thongBao").value("Tài khoản đã bị khóa"));
        mvc.perform(get("/api/auth/toi").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenPhatHanhTruocLanDoiMatKhauBiTuChoi() throws Exception {
        String email = emailMoi();
        dangKy(email);
        suaCsdl("UPDATE nguoi_dung SET doi_mat_khau_luc = now() - interval '30 seconds' WHERE email = ?", email);
        Long id = jdbc.queryForObject("SELECT id FROM nguoi_dung WHERE email = ?", Long.class, email);

        Instant truoc = Instant.now().minus(1, ChronoUnit.MINUTES);
        String tokenCu = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().issuer(JwtService.NHA_PHAT_HANH).subject(id.toString())
                        .issuedAt(truoc).expiresAt(truoc.plus(1, ChronoUnit.DAYS))
                        .claim(JwtService.CLAIM_VAI_TRO, "khach_hang").build())).getTokenValue();
        mvc.perform(get("/api/auth/toi").header("Authorization", "Bearer " + tokenCu))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.thongBao").value("Phiên đăng nhập không hợp lệ hoặc đã hết hạn"));

        String tokenMoi = JsonPath.read(mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"matKhau\":\"matkhau123\"}".formatted(email)))
                .andReturn().getResponse().getContentAsString(), "$.token");
        mvc.perform(get("/api/auth/toi").header("Authorization", "Bearer " + tokenMoi)).andExpect(status().isOk());
    }

    @Test
    void tokenCuConTrongTrinhDuyetKhongChanDangNhap() throws Exception {
        String email = emailMoi();
        dangKy(email);
        mvc.perform(post("/api/auth/dang-nhap").header("Authorization", "Bearer token.da.het.han")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"matKhau\":\"matkhau123\"}".formatted(email)))
                .andExpect(status().isOk());
    }

    @Test
    void tokenCuaTaiKhoanKhongConTonTaiBiTuChoi() throws Exception {
        String email = emailMoi();
        String token = dangKy(email);
        suaCsdl("DELETE FROM nguoi_dung WHERE email = ?", email);
        mvc.perform(get("/api/auth/toi").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}

package com.techgear.chung.baomat;

import com.techgear.chung.CauHinhChung;
import com.techgear.chung.loi.XuLyLoiToanCuc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BaoMatTest.ControllerThu.class)
@Import({BaoMatConfig.class, JsonLoiBaoMat.class, JwtService.class, CauHinhChung.class, XuLyLoiToanCuc.class,
        BaoMatTest.ControllerThu.class})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.jwt.khoa-bi-mat=khoa-kiem-thu-du-dai-it-nhat-32-ky-tu-0123456789",
        "app.jwt.thoi-han=1h",
        "app.cors.allowed-origins=http://localhost:5173"
})
class BaoMatTest {

    @RestController
    static class ControllerThu {
        @GetMapping("/api/thu")
        String cuaToi(@AuthenticationPrincipal Jwt jwt) {
            return jwt.getSubject();
        }

        @GetMapping("/api/admin/thu")
        String quanTri() {
            return "ok";
        }
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    JwtService jwtService;
    @Autowired
    JwtEncoder jwtEncoder;

    @Test
    void chuaDangNhapLa401Json() throws Exception {
        mvc.perform(get("/api/thu"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.trangThai").value(401))
                .andExpect(jsonPath("$.thongBao").value("Vui lòng đăng nhập"));
    }

    @Test
    void tokenSaiLa401Json() throws Exception {
        mvc.perform(get("/api/thu").header("Authorization", "Bearer khong.phai.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.thongBao").value("Phiên đăng nhập không hợp lệ hoặc đã hết hạn"));
    }

    @Test
    void tokenHetHanLa401() throws Exception {
        Instant phatHanh = Instant.now().minusSeconds(3 * 3600);
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(JwtService.NHA_PHAT_HANH).subject("7")
                .issuedAt(phatHanh).expiresAt(phatHanh.plusSeconds(3600)).claim("vaiTro", "khach_hang").build();
        String hetHan = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        mvc.perform(get("/api/thu").header("Authorization", "Bearer " + hetHan))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenHopLeDocDuocMaNguoiDung() throws Exception {
        mvc.perform(get("/api/thu").header("Authorization", "Bearer " + jwtService.taoToken(7L, "khach_hang")))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    void khachHangVaoTrangQuanTriLa403Json() throws Exception {
        mvc.perform(get("/api/admin/thu").header("Authorization", "Bearer " + jwtService.taoToken(7L, "khach_hang")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.thongBao").value("Bạn không có quyền thực hiện thao tác này"));
    }

    @Test
    void quanTriVaoTrangQuanTriDuoc() throws Exception {
        mvc.perform(get("/api/admin/thu").header("Authorization", "Bearer " + jwtService.taoToken(1L, "quan_tri")))
                .andExpect(status().isOk());
    }

    @Test
    void corsChoPhepOriginDaCauHinhVaTuChoiOriginLa() throws Exception {
        mvc.perform(options("/api/thu").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/thu").header("Origin", "http://ke-xau.vn")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}

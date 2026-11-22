package com.techgear.taikhoan;

import com.techgear.chung.ThoiGian;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RecordApplicationEvents
class DatLaiMatKhauIT extends KiemThuTaiKhoan {

    static final String DA_GUI = "Nếu email đã đăng ký, liên kết đặt lại mật khẩu đã được gửi";
    static final String MA_SAI = "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn";

    @Autowired
    ApplicationEvents suKien;

    static String sha256(String s) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    }

    void quenMatKhau(String email) throws Exception {
        mvc.perform(post("/api/auth/quen-mat-khau").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thongBao").value(DA_GUI));
    }

    void taoMa(String email, String ma, int hetHanSauPhut) {
        suaCsdl("INSERT INTO ma_dat_lai_mat_khau (nguoi_dung_id, ma_bam, het_han_luc) "
                + "SELECT id, SHA2(?, 256), now() + INTERVAL ? MINUTE FROM nguoi_dung WHERE email = ?",
                ma, hetHanSauPhut, email);
    }

    @Test
    void quenMatKhauChiLuuBamCuaMaVaGuiLienKet() throws Exception {
        String email = emailMoi();
        dangKy(email);
        quenMatKhau(email.toUpperCase());

        List<YeuCauDatLaiMatKhau> ds = suKien.stream(YeuCauDatLaiMatKhau.class).toList();
        assertEquals(1, ds.size());
        assertEquals(email, ds.getFirst().email());
        String lienKet = ds.getFirst().lienKet();
        assertTrue(lienKet.startsWith("http://localhost:5173/dat-lai-mat-khau?ma="), lienKet);
        String ma = lienKet.substring(lienKet.indexOf("ma=") + 3);
        assertTrue(ma.length() >= 43, "mã ngẫu nhiên 256 bit");

        Map<String, Object> dong = jdbc.queryForMap(
                "SELECT ma_bam, het_han_luc, da_su_dung FROM ma_dat_lai_mat_khau WHERE nguoi_dung_id = ?", idCua(email));
        assertEquals(sha256(ma), dong.get("ma_bam"));
        assertEquals(false, dong.get("da_su_dung"));
        // DATETIME lưu giờ Việt Nam
        LocalDateTime hetHan = (LocalDateTime) dong.get("het_han_luc");
        long phut = Duration.between(LocalDateTime.now(ThoiGian.VIET_NAM), hetHan).toMinutes();
        assertTrue(phut >= 14 && phut <= 15, "hết hạn sau 15 phút: " + phut);
    }

    @Test
    void emailChuaDangKyVanTraCungThongBaoVaKhongGuiGi() throws Exception {
        quenMatKhau(emailMoi());
        assertEquals(0, suKien.stream(YeuCauDatLaiMatKhau.class).count());
    }

    @Test
    void cungEmailGuiLaiTrong60GiayBi429() throws Exception {
        String email = emailMoi();
        quenMatKhau(email);
        mvc.perform(post("/api/auth/quen-mat-khau").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(email)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void datLaiMatKhauThanhCongThuHoiTokenCuVaMaChiDungMotLan() throws Exception {
        String email = emailMoi();
        dangKy(email);
        String tokenCu = tokenCu(idCua(email));
        taoMa(email, "ma-thu-1", 10);
        taoMa(email, "ma-thu-2", 10);

        mvc.perform(post("/api/auth/dat-lai-mat-khau").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ma\":\"ma-thu-1\",\"matKhauMoi\":\"matkhaumoi789\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thongBao").value("Đặt lại mật khẩu thành công, vui lòng đăng nhập"));
        em.flush();
        em.clear();

        mvc.perform(get("/api/auth/toi").header("Authorization", bearer(tokenCu))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"matKhau\":\"matkhaumoi789\"}".formatted(email)))
                .andExpect(status().isOk());
        // dùng lại mã vừa dùng, hoặc mã khác cấp trước đó → đều hết hiệu lực
        for (String ma : new String[]{"ma-thu-1", "ma-thu-2"}) {
            mvc.perform(post("/api/auth/dat-lai-mat-khau").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"ma\":\"%s\",\"matKhauMoi\":\"matkhaukhac000\"}".formatted(ma)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.thongBao").value(MA_SAI));
        }
    }

    @Test
    void maHetHanHoacKhongTonTaiLa400() throws Exception {
        String email = emailMoi();
        dangKy(email);
        taoMa(email, "ma-het-han", -1);
        for (String ma : new String[]{"ma-het-han", "ma-khong-co"}) {
            mvc.perform(post("/api/auth/dat-lai-mat-khau").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"ma\":\"%s\",\"matKhauMoi\":\"matkhaumoi789\"}".formatted(ma)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.thongBao").value(MA_SAI));
        }
        assertFalse(jdbc.queryForObject("SELECT da_su_dung FROM ma_dat_lai_mat_khau WHERE nguoi_dung_id = ?",
                Boolean.class, idCua(email)));
    }

    @Test
    void matKhauMoiYeuLa400TheoTruong() throws Exception {
        mvc.perform(post("/api/auth/dat-lai-mat-khau").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ma\":\"bat-ky\",\"matKhauMoi\":\"yeu\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.matKhauMoi").exists());
    }
}

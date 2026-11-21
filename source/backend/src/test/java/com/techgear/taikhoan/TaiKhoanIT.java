package com.techgear.taikhoan;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaiKhoanIT extends KiemThuTaiKhoan {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Test
    void xemVaSuaHoSo() throws Exception {
        String email = emailMoi();
        String token = dangKy(email);

        mvc.perform(get("/api/tai-khoan").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.hoTen").value("Nguyễn Văn An"));

        mvc.perform(put("/api/tai-khoan").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hoTen":"  Lê Thị Cúc ","soDienThoai":"0987654321","gioiTinh":"nu","ngaySinh":"2000-05-20"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hoTen").value("Lê Thị Cúc"))
                .andExpect(jsonPath("$.soDienThoai").value("0987654321"))
                .andExpect(jsonPath("$.gioiTinh").value("nu"))
                .andExpect(jsonPath("$.ngaySinh").value("2000-05-20"))
                .andExpect(jsonPath("$.email").value(email));

        // ghi đè cả 4 trường: bỏ trống thì xóa giá trị cũ
        mvc.perform(put("/api/tai-khoan").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hoTen\":\"Lê Thị Cúc\",\"soDienThoai\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.soDienThoai").doesNotExist())
                .andExpect(jsonPath("$.gioiTinh").doesNotExist())
                .andExpect(jsonPath("$.ngaySinh").doesNotExist());
    }

    @Test
    void suaHoSoSaiDuLieuLa400() throws Exception {
        String token = dangKy(emailMoi());
        mvc.perform(put("/api/tai-khoan").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hoTen\":\"\",\"ngaySinh\":\"%s\"}".formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.hoTen").exists())
                .andExpect(jsonPath("$.loi.ngaySinh").value("Ngày sinh phải trước hôm nay"));
        mvc.perform(put("/api/tai-khoan").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hoTen\":\"An\",\"gioiTinh\":\"khong-ro\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void taiKhoanCanDangNhap() throws Exception {
        mvc.perform(get("/api/tai-khoan")).andExpect(status().isUnauthorized());
    }

    @Test
    void doiMatKhauThuHoiTokenCuVaTraTokenMoi() throws Exception {
        String email = emailMoi();
        dangKy(email);
        String tokenCu = tokenCu(idCua(email));
        mvc.perform(get("/api/tai-khoan").header("Authorization", bearer(tokenCu))).andExpect(status().isOk());

        mvc.perform(put("/api/tai-khoan/mat-khau").header("Authorization", bearer(tokenCu))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"matKhauHienTai\":\"saimatkhau1\",\"matKhauMoi\":\"matkhaumoi456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Mật khẩu hiện tại không đúng"));

        String json = mvc.perform(put("/api/tai-khoan/mat-khau").header("Authorization", bearer(tokenCu))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"matKhauHienTai\":\"matkhau123\",\"matKhauMoi\":\"matkhaumoi456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nguoiDung.email").value(email))
                .andReturn().getResponse().getContentAsString();
        em.flush();
        em.clear();

        mvc.perform(get("/api/tai-khoan").header("Authorization", bearer(tokenCu))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/tai-khoan").header("Authorization", bearer(JsonPath.read(json, "$.token"))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/dang-nhap").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"matKhau\":\"matkhaumoi456\"}".formatted(email)))
                .andExpect(status().isOk());
    }

    @Test
    void doiMatKhauMoiYeuLa400() throws Exception {
        String token = dangKy(emailMoi());
        mvc.perform(put("/api/tai-khoan/mat-khau").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"matKhauHienTai\":\"matkhau123\",\"matKhauMoi\":\"ngan1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.matKhauMoi").exists());
    }

    @Test
    void doiAnhDaiDienXoaAnhCu() throws Exception {
        String token = dangKy(emailMoi());
        String anh1 = JsonPath.read(mvc.perform(multipart("/api/tai-khoan/anh-dai-dien")
                        .file(new MockMultipartFile("file", "a.png", "image/png", PNG))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anhDaiDien", startsWith("/uploads/anh-dai-dien/")))
                .andReturn().getResponse().getContentAsString(), "$.anhDaiDien");
        Path file1 = Path.of("target/uploads-it", anh1.substring("/uploads/".length()));
        assertTrue(Files.exists(file1));

        String anh2 = JsonPath.read(mvc.perform(multipart("/api/tai-khoan/anh-dai-dien")
                        .file(new MockMultipartFile("file", "b.png", "image/png", PNG))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.anhDaiDien");
        assertFalse(Files.exists(file1));
        assertTrue(Files.exists(Path.of("target/uploads-it", anh2.substring("/uploads/".length()))));
    }

    @Test
    void anhDaiDienKhongPhaiAnhLa415() throws Exception {
        String token = dangKy(emailMoi());
        mvc.perform(multipart("/api/tai-khoan/anh-dai-dien")
                        .file(new MockMultipartFile("file", "a.png", "image/png", "khong phai anh".getBytes()))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isUnsupportedMediaType());
    }
}

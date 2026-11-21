package com.techgear.taikhoan;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DiaChiIT extends KiemThuTaiKhoan {

    static String diaChi(String ten, Boolean laMacDinh) {
        return """
                {"tenNguoiNhan":"%s","soDienThoai":"0912345678","tinhThanh":"Thành phố Hồ Chí Minh",
                 "phuongXa":"Phường Bến Thành","diaChiChiTiet":"12 Lê Lợi","laMacDinh":%s}
                """.formatted(ten, laMacDinh);
    }

    Integer them(String token, String ten, Boolean laMacDinh) throws Exception {
        return JsonPath.read(mvc.perform(post("/api/tai-khoan/dia-chi").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(diaChi(ten, laMacDinh)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    long soMacDinh(String email) {
        em.flush();
        return jdbc.queryForObject("SELECT count(*) FROM dia_chi WHERE la_mac_dinh AND nguoi_dung_id = ?",
                Long.class, idCua(email));
    }

    @Test
    void diaChiDauTienTuLaMacDinhVaDanhSachMacDinhDungDau() throws Exception {
        String email = emailMoi();
        String token = dangKy(email);
        mvc.perform(post("/api/tai-khoan/dia-chi").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(diaChi("Nhà", false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tenNguoiNhan").value("Nhà"))
                .andExpect(jsonPath("$.phuongXa").value("Phường Bến Thành"))
                .andExpect(jsonPath("$.laMacDinh").value(true));
        them(token, "Công ty", null);
        them(token, "Nhà bố mẹ", null);

        mvc.perform(get("/api/tai-khoan/dia-chi").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].tenNguoiNhan").value("Nhà"))
                .andExpect(jsonPath("$[0].laMacDinh").value(true))
                .andExpect(jsonPath("$[1].tenNguoiNhan").value("Nhà bố mẹ"))
                .andExpect(jsonPath("$[2].laMacDinh").value(false));
        assertEquals(1, soMacDinh(email));
    }

    @Test
    void themDiaChiMacDinhMoiThiBoMacDinhCu() throws Exception {
        String email = emailMoi();
        String token = dangKy(email);
        them(token, "Cũ", null);
        them(token, "Mới", true);
        mvc.perform(get("/api/tai-khoan/dia-chi").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$[0].tenNguoiNhan").value("Mới"))
                .andExpect(jsonPath("$[0].laMacDinh").value(true))
                .andExpect(jsonPath("$[1].laMacDinh").value(false));
        assertEquals(1, soMacDinh(email));
    }

    @Test
    void toiDa10DiaChi() throws Exception {
        String token = dangKy(emailMoi());
        for (int i = 1; i <= 10; i++) {
            them(token, "Địa chỉ " + i, null);
        }
        mvc.perform(post("/api/tai-khoan/dia-chi").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(diaChi("Thứ 11", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Chỉ lưu tối đa 10 địa chỉ"));
    }

    @Test
    void suaDiaChiVaKhongBoDuocMacDinhBangFalse() throws Exception {
        String email = emailMoi();
        String token = dangKy(email);
        Integer id = them(token, "Nhà", null);
        Integer id2 = them(token, "Công ty", null);

        mvc.perform(put("/api/tai-khoan/dia-chi/" + id).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(diaChi("Nhà riêng", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenNguoiNhan").value("Nhà riêng"))
                .andExpect(jsonPath("$.laMacDinh").value(true));

        mvc.perform(put("/api/tai-khoan/dia-chi/" + id2).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(diaChi("Công ty", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laMacDinh").value(true));
        assertEquals(1, soMacDinh(email));
    }

    @Test
    void xoaDiaChiMacDinhThiDiaChiMoiNhatConLaiThanhMacDinh() throws Exception {
        String email = emailMoi();
        String token = dangKy(email);
        Integer macDinh = them(token, "Mặc định", null);
        them(token, "Cũ hơn", null);
        Integer moiNhat = them(token, "Mới nhất", null);

        mvc.perform(delete("/api/tai-khoan/dia-chi/" + macDinh).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/tai-khoan/dia-chi").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(moiNhat))
                .andExpect(jsonPath("$[0].laMacDinh").value(true));
        assertEquals(1, soMacDinh(email));
    }

    @Test
    void datMacDinh() throws Exception {
        String email = emailMoi();
        String token = dangKy(email);
        them(token, "Nhà", null);
        Integer id = them(token, "Công ty", null);
        mvc.perform(patch("/api/tai-khoan/dia-chi/" + id + "/mac-dinh").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.laMacDinh").value(true));
        assertEquals(1, soMacDinh(email));
    }

    @Test
    void diaChiCuaNguoiKhacLa404() throws Exception {
        Integer cuaA = them(dangKy(emailMoi()), "Của A", null);
        String tokenB = dangKy(emailMoi());

        mvc.perform(put("/api/tai-khoan/dia-chi/" + cuaA).header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON).content(diaChi("Chiếm", true)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy địa chỉ"));
        mvc.perform(delete("/api/tai-khoan/dia-chi/" + cuaA).header("Authorization", bearer(tokenB)))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/tai-khoan/dia-chi/" + cuaA + "/mac-dinh").header("Authorization", bearer(tokenB)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/tai-khoan/dia-chi").header("Authorization", bearer(tokenB)))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void duLieuSaiLa400TheoTruong() throws Exception {
        String token = dangKy(emailMoi());
        mvc.perform(post("/api/tai-khoan/dia-chi").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenNguoiNhan\":\"\",\"soDienThoai\":\"123\",\"tinhThanh\":\"\",\"phuongXa\":\"\",\"diaChiChiTiet\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.tenNguoiNhan").exists())
                .andExpect(jsonPath("$.loi.soDienThoai").value("Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"))
                .andExpect(jsonPath("$.loi.tinhThanh").exists())
                .andExpect(jsonPath("$.loi.phuongXa").exists())
                .andExpect(jsonPath("$.loi.diaChiChiTiet").exists());
    }
}

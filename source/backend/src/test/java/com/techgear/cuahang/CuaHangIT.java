package com.techgear.cuahang;

import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CuaHangIT extends KiemThuTichHop {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void thuongHieuChiGomDangHienThiSapTheoTen() throws Exception {
        TaoDuLieuSanPham tao = new TaoDuLieuSanPham(jdbc);
        tao.thuongHieu("Ugreen", "ugreen", true);
        tao.thuongHieu("Anker", "anker", true);
        tao.thuongHieu("Ẩn", "an", false);
        long xoa = tao.thuongHieu("Đã xóa", "da-xoa", true);
        tao.sua("UPDATE thuong_hieu SET deleted_at = now() WHERE id = ?", xoa);

        mvc.perform(get("/api/thuong-hieu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].slug", contains("anker", "ugreen")))
                .andExpect(jsonPath("$[0].ten").value("Anker"))
                .andExpect(jsonPath("$[0].logo").doesNotExist());
    }

    @Test
    void bannerChiGomDangBatVaTrongKhungGio() throws Exception {
        jdbc.update("""
                INSERT INTO banner (tieu_de, hinh_anh, thu_tu, hien_thi, bat_dau_luc, ket_thuc_luc) VALUES
                  ('Không giới hạn', '/uploads/banner/a.png', 2, TRUE, NULL, NULL),
                  ('Đang chạy', '/uploads/banner/b.png', 1, TRUE, now() - interval '1 day', now() + interval '1 day'),
                  ('Chưa tới giờ', '/uploads/banner/c.png', 0, TRUE, now() + interval '1 hour', NULL),
                  ('Đã hết hạn', '/uploads/banner/d.png', 0, TRUE, NULL, now() - interval '1 hour'),
                  ('Đang tắt', '/uploads/banner/e.png', 0, FALSE, NULL, NULL)
                """);

        mvc.perform(get("/api/banner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tieuDe", contains("Đang chạy", "Không giới hạn")))
                .andExpect(jsonPath("$[0].hinhAnh").value("/uploads/banner/b.png"))
                .andExpect(jsonPath("$[0].lienKet").doesNotExist());
    }

    @Test
    void caiDatTraDangKhoaGiaTri() throws Exception {
        mvc.perform(get("/api/cai-dat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ten_cua_hang").value("TechGear"))
                .andExpect(jsonPath("$.phi_van_chuyen").value("30000"))
                .andExpect(jsonPath("$.mien_phi_van_chuyen_tu").value("500000"));
    }
}

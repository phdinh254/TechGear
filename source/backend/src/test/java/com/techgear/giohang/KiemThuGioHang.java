package com.techgear.giohang;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Tiện ích chung: một khách hàng mới đăng ký và một danh mục để tạo sản phẩm. */
abstract class KiemThuGioHang extends KiemThuTichHop {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    TaoDuLieuSanPham tao;
    long danhMuc;
    String token;

    @BeforeEach
    void chuanBiChung() throws Exception {
        tao = new TaoDuLieuSanPham(jdbc);
        danhMuc = tao.danhMuc("Phụ kiện", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        token = dangKy();
    }

    String dangKy() throws Exception {
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Khách giỏ hàng","email":"gh-%s@vi-du.vn","matKhau":"matkhau123"}
                        """.formatted(TaoDuLieuSanPham.hauTo())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    /** Sản phẩm một biến thể; trả id biến thể. */
    long bienThe(String ten, long gia, int ton) {
        long sp = tao.sanPham(ten, "sp-" + TaoDuLieuSanPham.hauTo(), danhMuc, null);
        return tao.bienThe(sp, "SKU-" + TaoDuLieuSanPham.hauTo(), gia, null, ton);
    }

    long sanPhamCua(long bienTheId) {
        return jdbc.queryForObject("SELECT san_pham_id FROM bien_the WHERE id = ?", Long.class, bienTheId);
    }
}

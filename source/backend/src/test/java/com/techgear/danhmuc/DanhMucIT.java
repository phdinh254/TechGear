package com.techgear.danhmuc;

import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DanhMucIT extends KiemThuTichHop {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    TaoDuLieuSanPham tao;

    @BeforeEach
    void chuanBi() {
        tao = new TaoDuLieuSanPham(jdbc);
    }

    @Test
    void cayHaiCapSapTheoThuTuChiGomDanhMucDangHienThi() throws Exception {
        long sac = tao.danhMuc("Sạc", "sac", null, 2, true);
        long amThanh = tao.danhMuc("Âm thanh", "am-thanh", null, 1, true);
        tao.danhMuc("Cáp sạc", "cap-sac", sac, 2, true);
        tao.danhMuc("Củ sạc", "cu-sac", sac, 1, true);
        tao.danhMuc("Sạc ẩn", "sac-an", sac, 0, false);
        long daXoa = tao.danhMuc("Đã xóa", "da-xoa", null, 0, true);
        tao.sua("UPDATE danh_muc SET deleted_at = now() WHERE id = ?", daXoa);
        // danh mục cha ẩn thì cả nhánh ẩn
        long chaAn = tao.danhMuc("Cha ẩn", "cha-an", null, 0, false);
        tao.danhMuc("Con của cha ẩn", "con-cha-an", chaAn, 0, true);

        mvc.perform(get("/api/danh-muc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].slug", contains("am-thanh", "sac")))
                .andExpect(jsonPath("$[0].id").value(amThanh))
                .andExpect(jsonPath("$[0].danhMucCons", empty()))
                .andExpect(jsonPath("$[1].ten").value("Sạc"))
                .andExpect(jsonPath("$[1].danhMucCons[*].slug", contains("cu-sac", "cap-sac")))
                .andExpect(jsonPath("$[1].danhMucCons[0].danhMucCons", empty()))
                .andExpect(jsonPath("$[1].moTa").doesNotExist());
    }

    @Test
    void tokenCuHetHanKhongChanApiCongKhai() throws Exception {
        mvc.perform(get("/api/danh-muc").header("Authorization", "Bearer token.da.het.han"))
                .andExpect(status().isOk());
    }
}

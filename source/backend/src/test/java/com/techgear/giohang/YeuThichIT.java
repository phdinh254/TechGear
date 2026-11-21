package com.techgear.giohang;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class YeuThichIT extends KiemThuGioHang {

    @Test
    void themBoXemDanhSachSauDungTruoc() throws Exception {
        long a = tao.sanPhamCoBan("Thích trước", danhMuc, null, 100_000);
        long b = tao.sanPhamCoBan("Thích sau", danhMuc, null, 200_000);

        mvc.perform(put("/api/yeu-thich/" + a).header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(put("/api/yeu-thich/" + b).header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(put("/api/yeu-thich/" + a).header("Authorization", token)).andExpect(status().isNoContent());

        mvc.perform(get("/api/yeu-thich").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].ten", contains("Thích sau", "Thích trước")))
                .andExpect(jsonPath("$[0].giaBan").value(200_000))
                .andExpect(jsonPath("$[0].conHang").value(true));

        mvc.perform(delete("/api/yeu-thich/" + b).header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/yeu-thich/" + b).header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/yeu-thich").header("Authorization", token))
                .andExpect(jsonPath("$[*].ten", contains("Thích trước")));
    }

    @Test
    void sanPhamAnKhongThemDuocVaKhongHienTrongDanhSach() throws Exception {
        long sp = tao.sanPhamCoBan("Sẽ bị ẩn", danhMuc, null, 100_000);
        mvc.perform(put("/api/yeu-thich/" + sp).header("Authorization", token)).andExpect(status().isNoContent());
        tao.sua("UPDATE san_pham SET hien_thi = FALSE WHERE id = ?", sp);

        mvc.perform(get("/api/yeu-thich").header("Authorization", token)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(put("/api/yeu-thich/" + sp).header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy sản phẩm"));
        mvc.perform(put("/api/yeu-thich/999999999").header("Authorization", token)).andExpect(status().isNotFound());
    }

    @Test
    void toiDa100SanPham() throws Exception {
        long nguoiDung = jdbc.queryForObject("SELECT max(id) FROM nguoi_dung", Long.class);
        for (int i = 0; i < 100; i++) {
            long sp = tao.sanPham("SP " + i, "yt-" + i + "-" + com.techgear.TaoDuLieuSanPham.hauTo(), danhMuc, null);
            jdbc.update("INSERT INTO yeu_thich (nguoi_dung_id, san_pham_id) VALUES (?, ?)", nguoiDung, sp);
        }
        long moi = tao.sanPhamCoBan("Thứ 101", danhMuc, null, 100_000);
        mvc.perform(put("/api/yeu-thich/" + moi).header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Danh sách yêu thích tối đa 100 sản phẩm"));
    }

    @Test
    void canDangNhap() throws Exception {
        mvc.perform(get("/api/yeu-thich")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/yeu-thich/1")).andExpect(status().isUnauthorized());
    }
}

package com.techgear.taikhoan;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuQuanTri;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Quản trị người dùng (khóa/mở) và đánh giá (duyệt/ẩn/xóa). */
class AdminNguoiDungDanhGiaIT extends KiemThuQuanTri {

    long idCua(String email) {
        return jdbc.queryForObject("SELECT id FROM nguoi_dung WHERE email = ?", Long.class, email);
    }

    @Test
    void timNguoiDungVaKhoaMo() throws Exception {
        String email = "tim-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn";
        String token = dangKy(email);
        long id = idCua(email);

        qt(get("/api/admin/nguoi-dung").param("tuKhoa", email.toUpperCase()), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noiDung[*].id", contains((int) id)))
                .andExpect(jsonPath("$.noiDung[0].vaiTro").value("khach_hang"))
                .andExpect(jsonPath("$.noiDung[0].trangThai").value("hoat_dong"))
                .andExpect(jsonPath("$.noiDung[0].soDonHang").value(0))
                .andExpect(jsonPath("$.noiDung[0].matKhau").doesNotExist());
        qt(get("/api/admin/nguoi-dung").param("tuKhoa", "nguoi dung thu").param("vaiTro", "quan_tri"), null)
                .andExpect(jsonPath("$.noiDung[*].id", contains((int) adminId)));

        qt(patch("/api/admin/nguoi-dung/" + id + "/trang-thai"), "{\"trangThai\":\"bi_khoa\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value("bi_khoa"));
        mvc.perform(get("/api/auth/toi").header("Authorization", token)).andExpect(status().isUnauthorized());
        qt(patch("/api/admin/nguoi-dung/" + id + "/trang-thai"), "{\"trangThai\":\"hoat_dong\"}")
                .andExpect(jsonPath("$.trangThai").value("hoat_dong"));
        mvc.perform(get("/api/auth/toi").header("Authorization", token)).andExpect(status().isOk());

        qt(patch("/api/admin/nguoi-dung/" + adminId + "/trang-thai"), "{\"trangThai\":\"bi_khoa\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Không thể tự khóa tài khoản của mình"));
        qt(patch("/api/admin/nguoi-dung/999999999/trang-thai"), "{\"trangThai\":\"bi_khoa\"}").andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/nguoi-dung").header("Authorization", khach)).andExpect(status().isForbidden());
    }

    @Test
    void duyetAnXoaDanhGia() throws Exception {
        long dm = tao.danhMuc("DM", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        long sp = tao.sanPham("Bàn phím", "ban-phim-" + TaoDuLieuSanPham.hauTo(), dm, null);
        long bt = tao.bienThe(sp, "BP-" + TaoDuLieuSanPham.hauTo(), 100_000, null, 10);
        String ma = JsonPath.read(mvc.perform(post("/api/don-hang").header("Authorization", khach).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dongs":[{"bienTheId":%d,"soLuong":1}],"tenNguoiNhan":"An","soDienThoai":"0912345678",
                                 "tinhThanh":"HCM","phuongXa":"Sài Gòn","diaChiChiTiet":"1","phuongThucThanhToan":"cod"}
                                """.formatted(bt)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.maDonHang");
        jdbc.update("UPDATE don_hang SET trang_thai = 'hoan_thanh' WHERE ma_don_hang = ?", ma);
        long dg = id(mvc.perform(post("/api/danh-gia").header("Authorization", khach).contentType(MediaType.APPLICATION_JSON)
                .content("{\"maDonHang\":\"%s\",\"sanPhamId\":%d,\"soSao\":4,\"noiDung\":\"Gõ êm\"}".formatted(ma, sp))));
        String slug = tao.slugSanPham(sp);

        qt(get("/api/admin/danh-gia").param("trangThai", "cho_duyet").param("sanPhamId", String.valueOf(sp)), null)
                .andExpect(jsonPath("$.noiDung[*].id", contains((int) dg)))
                .andExpect(jsonPath("$.noiDung[0].maDonHang").value(ma))
                .andExpect(jsonPath("$.noiDung[0].tenSanPham").value("Bàn phím"))
                .andExpect(jsonPath("$.noiDung[0].email").exists());
        qt(patch("/api/admin/danh-gia/" + dg), "{\"trangThai\":\"da_duyet\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value("da_duyet"));
        mvc.perform(get("/api/san-pham/" + slug)).andExpect(jsonPath("$.diemTrungBinh").value(4.0));
        qt(patch("/api/admin/danh-gia/" + dg), "{\"trangThai\":\"da_an\"}").andExpect(status().isOk());
        mvc.perform(get("/api/san-pham/" + slug)).andExpect(jsonPath("$.soDanhGia").value(0));
        qt(patch("/api/admin/danh-gia/" + dg), "{\"trangThai\":\"lung_tung\"}").andExpect(status().isBadRequest());
        qt(delete("/api/admin/danh-gia/" + dg), null).andExpect(status().isNoContent());
        qt(delete("/api/admin/danh-gia/" + dg), null).andExpect(status().isNotFound());
        mvc.perform(get("/api/danh-gia/co-the-viet").header("Authorization", khach))
                .andExpect(jsonPath("$[*].maDonHang", contains(ma)));
    }
}

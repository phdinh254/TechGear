package com.techgear.khuyenmai;

import com.techgear.KiemThuQuanTri;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Quản trị mã giảm giá, banner, cài đặt cửa hàng. */
class AdminKhuyenMaiCuaHangIT extends KiemThuQuanTri {

    static final String THOI_GIAN = "\"batDauLuc\":\"2026-01-01T00:00:00+07:00\",\"ketThucLuc\":\"2030-01-01T00:00:00+07:00\"";

    @Test
    void maGiamGiaThemSuaXoa() throws Exception {
        String ma = "sale" + TaoDuLieuSanPham.hauTo().substring(0, 4);
        long id = id(qt(post("/api/admin/ma-giam-gia"), """
                {"ma":"%s","moTa":"Giảm 15%%","loaiGiam":"phan_tram","giaTriGiam":15,"giamToiDa":50000,"tongLuotDung":10,%s}
                """.formatted(ma, THOI_GIAN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ma").value(ma.toUpperCase()))
                .andExpect(jsonPath("$.daDung").value(0))
                .andExpect(jsonPath("$.donToiThieu").value(0))
                .andExpect(jsonPath("$.kichHoat").value(true))
                .andExpect(jsonPath("$.batDauLuc").value("2026-01-01T00:00:00+07:00")));
        qt(post("/api/admin/ma-giam-gia"), "{\"ma\":\"%s\",\"loaiGiam\":\"so_tien\",\"giaTriGiam\":1,%s}".formatted(ma.toUpperCase(), THOI_GIAN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Mã giảm giá đã tồn tại"));
        qt(post("/api/admin/ma-giam-gia"), "{\"ma\":\"PT%s\",\"loaiGiam\":\"phan_tram\",\"giaTriGiam\":150,%s}".formatted(ma, THOI_GIAN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Giảm theo phần trăm tối đa 100"));
        qt(post("/api/admin/ma-giam-gia"), """
                {"ma":"TG%s","loaiGiam":"so_tien","giaTriGiam":1,"batDauLuc":"2026-02-01T00:00:00+07:00","ketThucLuc":"2026-01-01T00:00:00+07:00"}
                """.formatted(ma))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Thời gian kết thúc phải sau thời gian bắt đầu"));
        qt(post("/api/admin/ma-giam-gia"), "{\"ma\":\"có dấu\",\"loaiGiam\":\"so_tien\",\"giaTriGiam\":1,%s}".formatted(THOI_GIAN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.ma").exists());

        jdbc.update("UPDATE ma_giam_gia SET da_dung = 5 WHERE id = ?", id);
        qt(put("/api/admin/ma-giam-gia/" + id), "{\"ma\":\"%s\",\"loaiGiam\":\"phan_tram\",\"giaTriGiam\":15,\"tongLuotDung\":4,%s}".formatted(ma, THOI_GIAN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Tổng lượt không được nhỏ hơn số lượt đã dùng"));
        qt(put("/api/admin/ma-giam-gia/" + id), "{\"ma\":\"%s\",\"loaiGiam\":\"so_tien\",\"giaTriGiam\":20000,\"kichHoat\":false,%s}".formatted(ma, THOI_GIAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loaiGiam").value("so_tien"))
                .andExpect(jsonPath("$.daDung").value(5))
                .andExpect(jsonPath("$.kichHoat").value(false))
                .andExpect(jsonPath("$.tongLuotDung").doesNotExist());
        qt(get("/api/admin/ma-giam-gia").param("tuKhoa", ma), null)
                .andExpect(jsonPath("$.noiDung[*].id", contains((int) id)));
        qt(delete("/api/admin/ma-giam-gia/" + id), null).andExpect(status().isNoContent());
        qt(get("/api/admin/ma-giam-gia").param("tuKhoa", ma), null).andExpect(jsonPath("$.tongSoPhanTu").value(0));
        qt(post("/api/admin/ma-giam-gia"), "{\"ma\":\"%s\",\"loaiGiam\":\"so_tien\",\"giaTriGiam\":1,%s}".formatted(ma, THOI_GIAN))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/admin/ma-giam-gia").header("Authorization", khach)).andExpect(status().isForbidden());
    }

    @Test
    void bannerThemSuaXoaVaLienKetAnToan() throws Exception {
        long id = id(qt(post("/api/admin/banner"), "{\"tieuDe\":\"Sale 11.11\",\"hinhAnh\":\"/uploads/banner/a.png\",\"lienKet\":\"/san-pham?danhMuc=sac-cap\",\"thuTu\":1}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hienThi").value(true)));
        qt(post("/api/admin/banner"), "{\"hinhAnh\":\"/uploads/banner/b.png\",\"lienKet\":\"javascript:alert(1)\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.lienKet").exists());
        qt(put("/api/admin/banner/" + id), "{\"hinhAnh\":\"/uploads/banner/a2.png\",\"hienThi\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hinhAnh").value("/uploads/banner/a2.png"))
                .andExpect(jsonPath("$.tieuDe").doesNotExist())
                .andExpect(jsonPath("$.thuTu").value(1));
        mvc.perform(get("/api/banner")).andExpect(jsonPath("$[*].id", not(hasItem((int) id))));
        qt(get("/api/admin/banner"), null).andExpect(jsonPath("$[*].id", hasItem((int) id)));
        qt(delete("/api/admin/banner/" + id), null).andExpect(status().isNoContent());
        qt(delete("/api/admin/banner/" + id), null).andExpect(status().isNotFound());
    }

    @Test
    void caiDatChiKhoaCoSanVaKiemSo() throws Exception {
        qt(get("/api/admin/cai-dat"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.khoa == 'phi_van_chuyen')].giaTri").value("30000"));
        qt(put("/api/admin/cai-dat"), "{\"phi_van_chuyen\":\"25000\",\"hotline\":\"1800 0000\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.khoa == 'phi_van_chuyen')].giaTri").value("25000"));
        mvc.perform(get("/api/cai-dat")).andExpect(jsonPath("$.hotline").value("1800 0000"));
        qt(put("/api/admin/cai-dat"), "{\"phi_van_chuyen\":\"-1\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("phi_van_chuyen phải là số nguyên không âm"));
        qt(put("/api/admin/cai-dat"), "{\"jwt_secret\":\"x\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Khóa cấu hình không hợp lệ: jwt_secret"));
    }
}

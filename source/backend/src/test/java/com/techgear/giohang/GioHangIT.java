package com.techgear.giohang;

import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GioHangIT extends KiemThuGioHang {

    ResultActions them(long bienTheId, int soLuong) throws Exception {
        return mvc.perform(post("/api/gio-hang").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"bienTheId\":%d,\"soLuong\":%d}".formatted(bienTheId, soLuong)));
    }

    ResultActions sua(long bienTheId, int soLuong) throws Exception {
        return mvc.perform(put("/api/gio-hang/" + bienTheId).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"soLuong\":%d}".formatted(soLuong)));
    }

    @Test
    void themCongDonTraGioDungHinhDang() throws Exception {
        String h = TaoDuLieuSanPham.hauTo();
        long sp = tao.sanPham("Tai nghe X", "tai-nghe-x-" + h, danhMuc, null);
        long mau = jdbc.queryForObject(
                "INSERT INTO tuy_chon (san_pham_id, ten, loai) VALUES (?, 'Màu sắc', 'mau_sac') RETURNING id", Long.class, sp);
        long den = jdbc.queryForObject(
                "INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri) VALUES (?, 'Đen') RETURNING id", Long.class, mau);
        long bt = tao.bienThe(sp, "TNX-DEN-" + h, 500_000, 600_000L, 10);
        jdbc.update("INSERT INTO bien_the_gia_tri VALUES (?, ?)", bt, den);
        jdbc.update("""
                INSERT INTO hinh_anh_san_pham (san_pham_id, gia_tri_tuy_chon_id, duong_dan, la_anh_chinh)
                VALUES (?, NULL, '/uploads/san-pham/chinh.png', TRUE), (?, ?, '/uploads/san-pham/den.png', FALSE)
                """, sp, sp, den);
        long khac = bienThe("Cáp", 100_000, 10);

        them(bt, 2).andExpect(status().isOk());
        them(khac, 1).andExpect(status().isOk());
        them(bt, 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dongs", hasSize(2)))
                .andExpect(jsonPath("$.dongs[*].bienTheId", contains((int) khac, (int) bt)))
                .andExpect(jsonPath("$.dongs[1].soLuong").value(3))
                .andExpect(jsonPath("$.dongs[1].sanPhamId").value(sp))
                .andExpect(jsonPath("$.dongs[1].sanPhamSlug").value("tai-nghe-x-" + h))
                .andExpect(jsonPath("$.dongs[1].ten").value("Tai nghe X"))
                .andExpect(jsonPath("$.dongs[1].phanLoai").value("Đen"))
                .andExpect(jsonPath("$.dongs[1].anh").value("/uploads/san-pham/den.png"))
                .andExpect(jsonPath("$.dongs[1].sku").value("TNX-DEN-" + h))
                .andExpect(jsonPath("$.dongs[1].giaBan").value(500_000))
                .andExpect(jsonPath("$.dongs[1].giaGoc").value(600_000))
                .andExpect(jsonPath("$.dongs[1].tonKho").value(10))
                .andExpect(jsonPath("$.dongs[1].conBan").value(true))
                .andExpect(jsonPath("$.dongs[0].phanLoai").doesNotExist())
                .andExpect(jsonPath("$.dongs[0].anh").doesNotExist())
                .andExpect(jsonPath("$.tongSoLuong").value(4))
                .andExpect(jsonPath("$.tamTinh").value(1_600_000));

        mvc.perform(get("/api/gio-hang").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tongSoLuong").value(4));
    }

    @Test
    void khongVuotTonKhoTinhCaSoDaCoTrongGio() throws Exception {
        long bt = bienThe("Củ sạc", 300_000, 5);
        long het = bienThe("Hết hàng", 300_000, 0);

        them(bt, 4).andExpect(status().isOk());
        them(bt, 2)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Chỉ còn 5 sản phẩm trong kho"));
        them(het, 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Sản phẩm đã hết hàng"));
        sua(bt, 6).andExpect(status().isConflict());
        sua(bt, 5).andExpect(status().isOk()).andExpect(jsonPath("$.dongs[0].soLuong").value(5));
    }

    @Test
    void bienTheKhongConBanLa404() throws Exception {
        long an = bienThe("Biến thể tắt", 100_000, 5);
        tao.sua("UPDATE bien_the SET hien_thi = FALSE WHERE id = ?", an);
        long spAn = bienThe("Sản phẩm tắt", 100_000, 5);
        tao.sua("UPDATE san_pham SET hien_thi = FALSE WHERE id = ?", sanPhamCua(spAn));

        for (long id : new long[]{an, spAn, 999_999_999L}) {
            them(id, 1)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.thongBao").value("Phiên bản sản phẩm không còn bán"));
        }
    }

    @Test
    void suaXoaDong() throws Exception {
        long bt = bienThe("Chuột", 200_000, 10);
        sua(bt, 1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Sản phẩm không có trong giỏ hàng"));
        them(bt, 3);
        sua(bt, 1).andExpect(status().isOk()).andExpect(jsonPath("$.tamTinh").value(200_000));

        mvc.perform(delete("/api/gio-hang/" + bt).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dongs", hasSize(0)))
                .andExpect(jsonPath("$.tamTinh").value(0));
        mvc.perform(delete("/api/gio-hang/" + bt).header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void dongKhongConBanVanHienNhungKhongTinhTien() throws Exception {
        long bt = bienThe("Loa", 1_000_000, 10);
        long conBan = bienThe("Cáp", 100_000, 10);
        them(bt, 1);
        them(conBan, 2);
        tao.sua("UPDATE san_pham SET deleted_at = now() WHERE id = ?", sanPhamCua(bt));

        mvc.perform(get("/api/gio-hang").header("Authorization", token))
                .andExpect(jsonPath("$.dongs", hasSize(2)))
                .andExpect(jsonPath("$.dongs[?(@.bienTheId == %d)].conBan".formatted(bt)).value(false))
                .andExpect(jsonPath("$.tongSoLuong").value(2))
                .andExpect(jsonPath("$.tamTinh").value(200_000));
        sua(bt, 1).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Phiên bản sản phẩm không còn bán"));
    }

    @Test
    void gopGioTamCongDonCatTheoTonKhoBoDongKhongHopLe() throws Exception {
        long a = bienThe("A", 100_000, 5);
        long an = bienThe("Đã tắt", 100_000, 5);
        tao.sua("UPDATE bien_the SET hien_thi = FALSE WHERE id = ?", an);
        long het = bienThe("Hết", 100_000, 0);
        long d = bienThe("D", 50_000, 10);
        them(a, 2);

        mvc.perform(post("/api/gio-hang/gop").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{"bienTheId":%d,"soLuong":4},{"bienTheId":%d,"soLuong":1},{"bienTheId":%d,"soLuong":3},
                                 {"bienTheId":%d,"soLuong":2},{"bienTheId":%d,"soLuong":1},{"bienTheId":999999999,"soLuong":1}]
                                """.formatted(a, an, het, d, d)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dongs", hasSize(2)))
                .andExpect(jsonPath("$.dongs[?(@.bienTheId == %d)].soLuong".formatted(a)).value(5))
                .andExpect(jsonPath("$.dongs[?(@.bienTheId == %d)].soLuong".formatted(d)).value(3))
                .andExpect(jsonPath("$.tamTinh").value(650_000));
    }

    @Test
    void toiDa50Dong() throws Exception {
        long sp = tao.sanPham("Nhiều biến thể", "nhieu-" + TaoDuLieuSanPham.hauTo(), danhMuc, null);
        long[] ids = new long[51];
        for (int i = 0; i < 51; i++) {
            ids[i] = tao.bienThe(sp, "NBT-" + i + "-" + TaoDuLieuSanPham.hauTo(), 1_000, null, 5);
        }
        for (int i = 0; i < 50; i++) {
            them(ids[i], 1).andExpect(status().isOk());
        }
        them(ids[50], 1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Giỏ hàng tối đa 50 sản phẩm"));
        them(ids[0], 1).andExpect(status().isOk());
    }

    @Test
    void duLieuSaiLa400() throws Exception {
        long bt = bienThe("Củ sạc", 100_000, 5);
        them(bt, 0).andExpect(status().isBadRequest()).andExpect(jsonPath("$.loi.soLuong").exists());
        them(bt, 1000).andExpect(status().isBadRequest()).andExpect(jsonPath("$.loi.soLuong").exists());
        mvc.perform(post("/api/gio-hang").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"soLuong\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.bienTheId").exists());
        mvc.perform(post("/api/gio-hang/gop").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"bienTheId\":%d,\"soLuong\":0}]".formatted(bt)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void canDangNhapVaMoiNguoiMotGio() throws Exception {
        mvc.perform(get("/api/gio-hang")).andExpect(status().isUnauthorized());
        long bt = bienThe("Củ sạc", 100_000, 5);
        them(bt, 1);
        String nguoiKhac = dangKy();
        mvc.perform(get("/api/gio-hang").header("Authorization", nguoiKhac))
                .andExpect(jsonPath("$.dongs", hasSize(0)));
    }
}

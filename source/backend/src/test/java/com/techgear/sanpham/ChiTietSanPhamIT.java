package com.techgear.sanpham;

import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChiTietSanPhamIT extends KiemThuTichHop {

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
    void chiTietDungHopDongFrontendVaChiGomBienTheDangBan() throws Exception {
        String h = TaoDuLieuSanPham.hauTo();
        long dm = tao.danhMuc("Tai nghe", "tai-nghe-" + h, null, 0, true);
        long th = tao.thuongHieu("Sony", "sony-" + h, true);
        long sp = tao.sanPham("Tai nghe WH-1000XM5", "wh-" + h, dm, th);
        jdbc.update("UPDATE san_pham SET mo_ta_ngan = 'Chống ồn', mo_ta = '<p>Mô tả</p>', bao_hanh_thang = 12 WHERE id = ?", sp);
        long mau = jdbc.queryForObject(
                "INSERT INTO tuy_chon (san_pham_id, ten, loai, thu_tu) VALUES (?, 'Màu sắc', 'mau_sac', 0) RETURNING id",
                Long.class, sp);
        long den = jdbc.queryForObject(
                "INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri, ma_mau, thu_tu) VALUES (?, 'Đen', '#000000', 0) RETURNING id",
                Long.class, mau);
        long bac = jdbc.queryForObject(
                "INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri, thu_tu) VALUES (?, 'Bạc', 1) RETURNING id",
                Long.class, mau);
        long btDen = tao.bienThe(sp, "WH-DEN-" + h, 8_000_000, 9_000_000L, 3);
        long btBac = tao.bienThe(sp, "WH-BAC-" + h, 7_500_000, null, 0);
        long btAn = tao.bienThe(sp, "WH-AN-" + h, 1_000, null, 9);
        jdbc.update("UPDATE bien_the SET hien_thi = FALSE WHERE id = ?", btAn);
        tao.capNhatGiaHienThi(sp);
        jdbc.update("INSERT INTO bien_the_gia_tri VALUES (?, ?), (?, ?)", btDen, den, btBac, bac);
        jdbc.update("INSERT INTO thong_so (san_pham_id, ten, gia_tri, thu_tu) VALUES (?, 'Pin', '30 giờ', 2), (?, 'Kết nối', 'Bluetooth 5.2', 1)", sp, sp);
        jdbc.update("""
                INSERT INTO hinh_anh_san_pham (san_pham_id, gia_tri_tuy_chon_id, duong_dan, mo_ta, thu_tu, la_anh_chinh)
                VALUES (?, ?, '/uploads/san-pham/den.png', 'Màu đen', 0, FALSE), (?, NULL, '/uploads/san-pham/chinh.png', NULL, 3, TRUE)
                """, sp, den, sp);

        mvc.perform(get("/api/san-pham/wh-" + h).header("Authorization", "Bearer token.da.het.han"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sp))
                .andExpect(jsonPath("$.ten").value("Tai nghe WH-1000XM5"))
                .andExpect(jsonPath("$.slug").value("wh-" + h))
                .andExpect(jsonPath("$.moTaNgan").value("Chống ồn"))
                .andExpect(jsonPath("$.moTa").value("<p>Mô tả</p>"))
                .andExpect(jsonPath("$.giaBan").value(7_500_000))
                .andExpect(jsonPath("$.giaGoc").doesNotExist())
                .andExpect(jsonPath("$.baoHanhThang").value(12))
                .andExpect(jsonPath("$.conHang").value(true))
                .andExpect(jsonPath("$.danhMuc.ten").value("Tai nghe"))
                .andExpect(jsonPath("$.danhMuc.slug").value("tai-nghe-" + h))
                .andExpect(jsonPath("$.thuongHieu.ten").value("Sony"))
                .andExpect(jsonPath("$.hinhAnhs[*].duongDan", contains("/uploads/san-pham/chinh.png", "/uploads/san-pham/den.png")))
                .andExpect(jsonPath("$.hinhAnhs[0].laAnhChinh").value(true))
                .andExpect(jsonPath("$.hinhAnhs[1].giaTriTuyChonId").value(den))
                .andExpect(jsonPath("$.hinhAnhs[1].moTa").value("Màu đen"))
                .andExpect(jsonPath("$.thongSos[*].ten", contains("Kết nối", "Pin")))
                .andExpect(jsonPath("$.thongSos[1].giaTri").value("30 giờ"))
                .andExpect(jsonPath("$.tuyChons", hasSize(1)))
                .andExpect(jsonPath("$.tuyChons[0].id").value(mau))
                .andExpect(jsonPath("$.tuyChons[0].ten").value("Màu sắc"))
                .andExpect(jsonPath("$.tuyChons[0].loai").value("mau_sac"))
                .andExpect(jsonPath("$.tuyChons[0].giaTris[*].giaTri", contains("Đen", "Bạc")))
                .andExpect(jsonPath("$.tuyChons[0].giaTris[0].maMau").value("#000000"))
                .andExpect(jsonPath("$.tuyChons[0].giaTris[1].maMau").doesNotExist())
                .andExpect(jsonPath("$.bienThes[*].id", containsInAnyOrder((int) btDen, (int) btBac)))
                .andExpect(jsonPath("$.bienThes[?(@.id == %d)].sku".formatted(btDen)).value("WH-DEN-" + h))
                .andExpect(jsonPath("$.bienThes[?(@.id == %d)].giaGoc".formatted(btDen)).value(9_000_000))
                .andExpect(jsonPath("$.bienThes[?(@.id == %d)].soLuongTon".formatted(btBac)).value(0))
                .andExpect(jsonPath("$.bienThes[?(@.id == %d)].giaTriIds[0]".formatted(btDen)).value((int) den));
    }

    @Test
    void sanPhamKhongCoTuyChonVanCoMangRong() throws Exception {
        long dm = tao.danhMuc("DM", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        long sp = tao.sanPhamCoBan("Đơn giản", dm, null, 100_000);

        mvc.perform(get("/api/san-pham/" + tao.slugSanPham(sp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tuyChons", hasSize(0)))
                .andExpect(jsonPath("$.hinhAnhs", hasSize(0)))
                .andExpect(jsonPath("$.thongSos", hasSize(0)))
                .andExpect(jsonPath("$.thuongHieu").doesNotExist())
                .andExpect(jsonPath("$.bienThes[0].giaTriIds", hasSize(0)));
    }

    @Test
    void xemChiTietTangLuotXem() throws Exception {
        long dm = tao.danhMuc("DM", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        long sp = tao.sanPhamCoBan("Xem nhiều", dm, null, 100_000);
        String slug = tao.slugSanPham(sp);

        mvc.perform(get("/api/san-pham/" + slug)).andExpect(status().isOk());
        mvc.perform(get("/api/san-pham/" + slug)).andExpect(status().isOk());

        assertThat(jdbc.queryForObject("SELECT luot_xem FROM san_pham WHERE id = ?", Integer.class, sp)).isEqualTo(2);
    }

    @Test
    void sanPhamKhongTonTaiHoacDangAnLa404() throws Exception {
        long dm = tao.danhMuc("DM", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        long an = tao.sanPhamCoBan("Đang ẩn", dm, null, 100_000);
        tao.sua("UPDATE san_pham SET hien_thi = FALSE WHERE id = ?", an);

        mvc.perform(get("/api/san-pham/khong-ton-tai-" + TaoDuLieuSanPham.hauTo()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy sản phẩm"));
        mvc.perform(get("/api/san-pham/" + tao.slugSanPham(an)))
                .andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT luot_xem FROM san_pham WHERE id = ?", Integer.class, an)).isZero();
    }
}

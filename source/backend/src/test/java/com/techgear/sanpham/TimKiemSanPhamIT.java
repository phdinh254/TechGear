package com.techgear.sanpham;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimKiemSanPhamIT extends KiemThuTichHop {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    TaoDuLieuSanPham tao;
    long danhMuc;
    String slugDanhMuc;

    @BeforeEach
    void chuanBi() {
        tao = new TaoDuLieuSanPham(jdbc);
        slugDanhMuc = "dm-" + TaoDuLieuSanPham.hauTo();
        danhMuc = tao.danhMuc("Phụ kiện", slugDanhMuc, null, 0, true);
    }

    @Test
    void tuKhoaKhongDauKhongPhanBietHoaThuongNhieuTu() throws Exception {
        long anker = tao.thuongHieu("Anker", "anker-" + TaoDuLieuSanPham.hauTo(), true);
        tao.sanPhamCoBan("Củ sạc nhanh 65W", danhMuc, anker, 500_000);
        tao.sanPhamCoBan("Đế sạc không dây", danhMuc, null, 300_000);
        tao.sanPhamCoBan("Tai nghe Bluetooth", danhMuc, anker, 900_000);

        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("tuKhoa", "SAC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noiDung[*].ten", containsInAnyOrder("Củ sạc nhanh 65W", "Đế sạc không dây")));
        // nhiều từ: phải chứa đủ các từ, tìm cả trong tên thương hiệu
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("tuKhoa", "  anker   sac "))
                .andExpect(jsonPath("$.noiDung[*].ten", contains("Củ sạc nhanh 65W")));
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("tuKhoa", "de"))
                .andExpect(jsonPath("$.noiDung[*].ten", contains("Đế sạc không dây")));
    }

    @Test
    void kyTuDacBietCuaLikeDuocTimNguyenVan() throws Exception {
        tao.sanPhamCoBan("Giảm 50% phí", danhMuc, null, 100_000);
        tao.sanPhamCoBan("Cáp 50 cm", danhMuc, null, 100_000);
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("tuKhoa", "50%"))
                .andExpect(jsonPath("$.noiDung[*].ten", contains("Giảm 50% phí")));
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("tuKhoa", "_"))
                .andExpect(jsonPath("$.tongSoPhanTu").value(0));
    }

    @Test
    void locDanhMucChaGomDanhMucConVaNhieuThuongHieu() throws Exception {
        long con = tao.danhMuc("Con", "con-" + TaoDuLieuSanPham.hauTo(), danhMuc, 0, true);
        String slugA = "a-" + TaoDuLieuSanPham.hauTo();
        String slugB = "b-" + TaoDuLieuSanPham.hauTo();
        long a = tao.thuongHieu("A", slugA, true);
        long b = tao.thuongHieu("B", slugB, true);
        long c = tao.thuongHieu("C", "c-" + TaoDuLieuSanPham.hauTo(), true);
        tao.sanPhamCoBan("Ở danh mục cha", danhMuc, a, 100_000);
        tao.sanPhamCoBan("Ở danh mục con", con, b, 100_000);
        tao.sanPhamCoBan("Thương hiệu khác", con, c, 100_000);

        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("thuongHieu", slugA + "," + slugB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noiDung[*].ten", containsInAnyOrder("Ở danh mục cha", "Ở danh mục con")))
                .andExpect(jsonPath("$.noiDung[?(@.ten == 'Ở danh mục cha')].tenThuongHieu").value("A"));
    }

    @Test
    void locGiaTheoTungBienTheVaSapXepTheoGiaHienThi() throws Exception {
        long nhieuGia = tao.sanPham("Nhiều phiên bản", "nhieu-" + TaoDuLieuSanPham.hauTo(), danhMuc, null);
        tao.bienThe(nhieuGia, "NG-1-" + TaoDuLieuSanPham.hauTo(), 200_000, 250_000L, 5);
        tao.bienThe(nhieuGia, "NG-2-" + TaoDuLieuSanPham.hauTo(), 800_000, null, 5);
        tao.sanPhamCoBan("Rẻ", danhMuc, null, 100_000);
        tao.sanPhamCoBan("Đắt", danhMuc, null, 1_000_000);

        // 700k–900k: chỉ biến thể 800k của "Nhiều phiên bản" khớp; giá hiển thị vẫn là giá rẻ nhất 200k
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("giaTu", "700000").param("giaDen", "900000"))
                .andExpect(jsonPath("$.noiDung[*].ten", contains("Nhiều phiên bản")))
                .andExpect(jsonPath("$.noiDung[0].giaBan").value(200_000))
                .andExpect(jsonPath("$.noiDung[0].giaGoc").value(250_000));
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("sapXep", "gia_tang"))
                .andExpect(jsonPath("$.noiDung[*].ten", contains("Rẻ", "Nhiều phiên bản", "Đắt")));
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("sapXep", "gia_giam"))
                .andExpect(jsonPath("$.noiDung[*].ten", contains("Đắt", "Nhiều phiên bản", "Rẻ")))
                .andExpect(jsonPath("$.noiDung[0].giaGoc").doesNotExist());
    }

    @Test
    void phoBienTheoLuotXemMoiNhatTheoNgayTao() throws Exception {
        long cu = tao.sanPhamCoBan("Cũ nhiều lượt xem", danhMuc, null, 100_000);
        tao.sanPhamCoBan("Mới", danhMuc, null, 100_000);
        tao.sua("UPDATE san_pham SET luot_xem = 50, created_at = now() - INTERVAL 1 DAY WHERE id = ?", cu);

        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc))
                .andExpect(jsonPath("$.noiDung[*].ten", contains("Mới", "Cũ nhiều lượt xem")));
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("sapXep", "pho_bien"))
                .andExpect(jsonPath("$.noiDung[*].ten", contains("Cũ nhiều lượt xem", "Mới")));
    }

    @Test
    void haiTrangLienNhauKhongTrungKhiCungGiaVaCungNgayTao() throws Exception {
        for (int i = 0; i < 7; i++) {
            tao.sanPhamCoBan("Cùng giá " + i, danhMuc, null, 100_000);
        }
        tao.sua("UPDATE san_pham SET created_at = '2026-01-01 00:00:00' WHERE danh_muc_id = ?", danhMuc);

        List<Integer> ids = new ArrayList<>();
        for (int trang = 0; trang < 3; trang++) {
            String json = mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc).param("sapXep", "gia_tang")
                            .param("kichThuoc", "3").param("trang", String.valueOf(trang)))
                    .andExpect(jsonPath("$.trang").value(trang))
                    .andExpect(jsonPath("$.kichThuoc").value(3))
                    .andExpect(jsonPath("$.tongSoPhanTu").value(7))
                    .andExpect(jsonPath("$.tongSoTrang").value(3))
                    .andReturn().getResponse().getContentAsString();
            ids.addAll(JsonPath.read(json, "$.noiDung[*].id"));
        }
        assertThat(ids).hasSize(7);
        assertThat(new HashSet<>(ids)).hasSize(7);
    }

    @Test
    void anSanPhamKhongHienThiDuocVaTinhConHangAnhChinh() throws Exception {
        long hien = tao.sanPhamCoBan("Còn hiển thị", danhMuc, null, 100_000);
        long an = tao.sanPhamCoBan("Đang tắt", danhMuc, null, 100_000);
        long xoa = tao.sanPhamCoBan("Đã xóa", danhMuc, null, 100_000);
        tao.sanPham("Chưa có biến thể", "chua-bt-" + TaoDuLieuSanPham.hauTo(), danhMuc, null);
        long btAn = tao.sanPham("Chỉ có biến thể tắt", "bt-an-" + TaoDuLieuSanPham.hauTo(), danhMuc, null);
        tao.bienThe(btAn, "BTAN-" + TaoDuLieuSanPham.hauTo(), 100_000, null, 5);
        long danhMucAn = tao.danhMuc("Ẩn", "an-" + TaoDuLieuSanPham.hauTo(), danhMuc, 0, false);
        tao.sanPhamCoBan("Trong danh mục ẩn", danhMucAn, null, 100_000);
        tao.sua("UPDATE san_pham SET hien_thi = FALSE WHERE id = ?", an);
        tao.sua("UPDATE san_pham SET deleted_at = now() WHERE id = ?", xoa);
        tao.sua("UPDATE bien_the SET hien_thi = FALSE WHERE san_pham_id = ?", btAn);
        tao.sua("UPDATE bien_the SET so_luong_ton = 0 WHERE san_pham_id = ?", hien);
        tao.sua("""
                INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, thu_tu, la_anh_chinh)
                VALUES (?, '/uploads/san-pham/phu.png', 0, FALSE), (?, '/uploads/san-pham/chinh.png', 5, TRUE)
                """, hien, hien);

        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc))
                .andExpect(jsonPath("$.noiDung", hasSize(1)))
                .andExpect(jsonPath("$.noiDung[0].ten").value("Còn hiển thị"))
                .andExpect(jsonPath("$.noiDung[0].conHang").value(false))
                .andExpect(jsonPath("$.noiDung[0].anhChinh").value("/uploads/san-pham/chinh.png"))
                .andExpect(jsonPath("$.noiDung[0].tenThuongHieu").doesNotExist());
    }

    @Test
    void thamSoSaiTra400TiengViet() throws Exception {
        mvc.perform(get("/api/san-pham").param("kichThuoc", "49"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.kichThuoc").value("Kích thước trang từ 1 đến 48"));
        mvc.perform(get("/api/san-pham").param("trang", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.trang").exists());
        mvc.perform(get("/api/san-pham").param("sapXep", "lung_tung"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Tham số 'sapXep' không hợp lệ"));
        mvc.perform(get("/api/san-pham").param("giaTu", "abc"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/san-pham").param("giaTu", "500").param("giaDen", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Giá từ phải nhỏ hơn hoặc bằng giá đến"));
        mvc.perform(get("/api/san-pham").param("tuKhoa", "a".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.tuKhoa").exists());
    }

    @Test
    void mangDinhDangPhanTrangMacDinh12() throws Exception {
        mvc.perform(get("/api/san-pham").param("danhMuc", slugDanhMuc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noiDung", hasSize(0)))
                .andExpect(jsonPath("$.trang").value(0))
                .andExpect(jsonPath("$.kichThuoc").value(12))
                .andExpect(jsonPath("$.tongSoPhanTu").value(0))
                .andExpect(jsonPath("$.tongSoTrang").value(0));
    }

    @Test
    void noiBatToiDa8NhieuLuotXemTruoc() throws Exception {
        tao.sua("UPDATE san_pham SET noi_bat = FALSE");
        for (int i = 0; i < 9; i++) {
            long id = tao.sanPhamCoBan("Nổi bật " + i, danhMuc, null, 100_000);
            tao.sua("UPDATE san_pham SET noi_bat = TRUE, luot_xem = ? WHERE id = ?", i, id);
        }
        tao.sanPhamCoBan("Không nổi bật", danhMuc, null, 100_000);

        mvc.perform(get("/api/san-pham/noi-bat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].ten").value("Nổi bật 8"))
                .andExpect(jsonPath("$[7].ten").value("Nổi bật 1"));
    }
}

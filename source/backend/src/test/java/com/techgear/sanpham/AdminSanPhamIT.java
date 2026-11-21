package com.techgear.sanpham;

import com.techgear.KiemThuQuanTri;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminSanPhamIT extends KiemThuQuanTri {

    long danhMuc;
    long sp;

    @BeforeEach
    void taoSanPham() throws Exception {
        danhMuc = tao.danhMuc("Tai nghe", "tai-nghe-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        sp = id(qt(post("/api/admin/san-pham"), """
                {"ten":"Tai nghe Không Dây X","danhMucId":%d,"moTaNgan":"Nhẹ",
                 "moTa":"<p onclick=\\"x()\\">Pin <b>30 giờ</b></p><script>alert(1)</script>","baoHanhThang":12,
                 "thongSos":[{"ten":"Pin","giaTri":"30 giờ"},{"ten":"Kết nối","giaTri":"BT 5.3"}]}
                """.formatted(danhMuc))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("tai-nghe-khong-day-x"))
                .andExpect(jsonPath("$.moTa").value("<p>Pin <b>30 giờ</b></p>"))
                .andExpect(jsonPath("$.thongSos[*].ten", contains("Pin", "Kết nối")))
                .andExpect(jsonPath("$.giaBan").value(0))
                .andExpect(jsonPath("$.hienThi").value(true))
                .andExpect(jsonPath("$.bienThes", hasSize(0))));
    }

    long giaTri(String ten) {
        return jdbc.queryForObject("""
                SELECT gt.id FROM gia_tri_tuy_chon gt JOIN tuy_chon tc ON tc.id = gt.tuy_chon_id
                WHERE tc.san_pham_id = ? AND gt.gia_tri = ?
                """, Long.class, sp, ten);
    }

    long themMauSac() throws Exception {
        qt(post("/api/admin/san-pham/" + sp + "/tuy-chon"),
                "{\"ten\":\"Màu sắc\",\"loai\":\"mau_sac\",\"giaTris\":[{\"giaTri\":\"Đen\",\"maMau\":\"#000000\"},{\"giaTri\":\"Trắng\"}]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tuyChons[0].giaTris[*].giaTri", contains("Đen", "Trắng")));
        return jdbc.queryForObject("SELECT id FROM tuy_chon WHERE san_pham_id = ?", Long.class, sp);
    }

    @Test
    void bienTheCapNhatGiaHienThiVaKiemToHop() throws Exception {
        themMauSac();
        long den = giaTri("Đen");
        long trang = giaTri("Trắng");

        qt(post("/api/admin/san-pham/" + sp + "/bien-the"),
                "{\"sku\":\"TNX-DEN\",\"giaBan\":200000,\"soLuongTon\":5,\"giaTriIds\":[%d]}".formatted(den))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.giaBan").value(200_000))
                .andExpect(jsonPath("$.bienThes[0].soLuongTon").value(5))
                .andExpect(jsonPath("$.bienThes[0].nguongCanhBao").value(5));
        assertThat(jdbc.queryForObject("SELECT so_luong_sau FROM lich_su_ton_kho WHERE loai = 'khoi_tao' AND bien_the_id = (SELECT id FROM bien_the WHERE sku = 'TNX-DEN')", Integer.class)).isEqualTo(5);
        qt(post("/api/admin/san-pham/" + sp + "/bien-the"),
                "{\"sku\":\"TNX-TRANG\",\"giaBan\":150000,\"giaGoc\":180000,\"giaTriIds\":[%d]}".formatted(trang))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.giaBan").value(150_000))
                .andExpect(jsonPath("$.giaGoc").value(180_000));

        qt(post("/api/admin/san-pham/" + sp + "/bien-the"),
                "{\"sku\":\"TNX-DEN-2\",\"giaBan\":1,\"giaTriIds\":[%d]}".formatted(den))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đã có biến thể với tổ hợp này"));
        qt(post("/api/admin/san-pham/" + sp + "/bien-the"), "{\"sku\":\"TNX-RONG\",\"giaBan\":1,\"giaTriIds\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Mỗi tùy chọn phải chọn đúng một giá trị"));
        qt(post("/api/admin/san-pham/" + sp + "/bien-the"),
                "{\"sku\":\"TNX-HAI\",\"giaBan\":1,\"giaTriIds\":[%d,%d]}".formatted(den, trang))
                .andExpect(status().isBadRequest());
        qt(post("/api/admin/san-pham/" + sp + "/bien-the"),
                "{\"sku\":\"tnx-den\",\"giaBan\":1,\"giaTriIds\":[%d]}".formatted(trang))
                .andExpect(status().isConflict());
        qt(post("/api/admin/san-pham/" + sp + "/bien-the"),
                "{\"sku\":\"TNX-GOC\",\"giaBan\":200,\"giaGoc\":100,\"giaTriIds\":[%d]}".formatted(trang))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Giá gốc phải lớn hơn hoặc bằng giá bán"));

        long btTrang = jdbc.queryForObject("SELECT id FROM bien_the WHERE sku = 'TNX-TRANG'", Long.class);
        qt(put("/api/admin/bien-the/" + btTrang), "{\"sku\":\"TNX-TRANG\",\"giaBan\":250000,\"hienThi\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.giaBan").value(200_000))
                .andExpect(jsonPath("$.giaGoc").doesNotExist());
        long btDen = jdbc.queryForObject("SELECT id FROM bien_the WHERE sku = 'TNX-DEN'", Long.class);
        qt(delete("/api/admin/bien-the/" + btDen), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bienThes[*].sku", contains("TNX-TRANG")))
                .andExpect(jsonPath("$.giaBan").value(250_000));
        mvc.perform(get("/api/san-pham/tai-nghe-khong-day-x"))
                .andExpect(jsonPath("$.bienThes[*].sku", contains("TNX-TRANG")))
                .andExpect(jsonPath("$.moTa").value("<p>Pin <b>30 giờ</b></p>"));
        // SKU của biến thể đã xóa được dùng lại
        qt(post("/api/admin/san-pham/" + sp + "/bien-the"),
                "{\"sku\":\"TNX-DEN\",\"giaBan\":300000,\"giaTriIds\":[%d]}".formatted(den))
                .andExpect(status().isCreated());
    }

    @Test
    void suaTuyChonKhongXoaGiaTriDangDung() throws Exception {
        long tc = themMauSac();
        long den = giaTri("Đen");
        long trang = giaTri("Trắng");
        qt(post("/api/admin/san-pham/" + sp + "/bien-the"),
                "{\"sku\":\"TC-DEN\",\"giaBan\":100000,\"giaTriIds\":[%d]}".formatted(den)).andExpect(status().isCreated());

        qt(put("/api/admin/tuy-chon/" + tc), "{\"ten\":\"Màu\",\"loai\":\"mau_sac\",\"giaTris\":[{\"id\":%d,\"giaTri\":\"Trắng\"}]}"
                .formatted(trang))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Giá trị \"Đen\" đang được dùng ở biến thể"));
        qt(put("/api/admin/tuy-chon/" + tc), """
                {"ten":"Màu","loai":"mau_sac","giaTris":[{"id":%d,"giaTri":"Đen nhám","maMau":"#111111"},{"giaTri":"Xanh"}]}
                """.formatted(den))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tuyChons[0].ten").value("Màu"))
                .andExpect(jsonPath("$.tuyChons[0].giaTris[*].giaTri", contains("Đen nhám", "Xanh")))
                .andExpect(jsonPath("$.bienThes[0].giaTriIds[0]").value((int) den));
        qt(delete("/api/admin/tuy-chon/" + tc), null).andExpect(status().isConflict());
        qt(post("/api/admin/san-pham/" + sp + "/tuy-chon"), "{\"ten\":\"Màu\",\"loai\":\"mau_sac\",\"giaTris\":[{\"giaTri\":\"A\"}]}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Tùy chọn đã tồn tại"));
    }

    @Test
    void anhChinhDuyNhatVaGiaTriPhaiThuocSanPham() throws Exception {
        themMauSac();
        long den = giaTri("Đen");
        long a1 = id(qt(post("/api/admin/san-pham/" + sp + "/hinh-anh"), "{\"duongDan\":\"/uploads/san-pham/1.png\",\"laAnhChinh\":true}")
                .andExpect(status().isCreated()));
        long a1Id = jdbc.queryForObject("SELECT id FROM hinh_anh_san_pham WHERE duong_dan = '/uploads/san-pham/1.png'", Long.class);
        assertThat(a1).isEqualTo(sp);
        qt(post("/api/admin/san-pham/" + sp + "/hinh-anh"),
                "{\"duongDan\":\"/uploads/san-pham/2.png\",\"laAnhChinh\":true,\"giaTriTuyChonId\":%d,\"moTa\":\"Màu đen\"}".formatted(den))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hinhAnhs[?(@.laAnhChinh == true)].duongDan", contains("/uploads/san-pham/2.png")));
        long khac = tao.danhMuc("K", "k-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        long spKhac = tao.sanPhamCoBan("Khác", khac, null, 1);
        long tcKhac = jdbc.queryForObject("INSERT INTO tuy_chon (san_pham_id, ten, loai) VALUES (?, 'Màu', 'mau_sac') RETURNING id", Long.class, spKhac);
        long gtKhac = jdbc.queryForObject("INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri) VALUES (?, 'Đỏ') RETURNING id", Long.class, tcKhac);
        qt(put("/api/admin/hinh-anh/" + a1Id), "{\"duongDan\":\"/uploads/san-pham/1.png\",\"giaTriTuyChonId\":%d}".formatted(gtKhac))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Giá trị tùy chọn không thuộc sản phẩm này"));
        qt(delete("/api/admin/hinh-anh/" + a1Id), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hinhAnhs", hasSize(1)));
    }

    @Test
    void danhSachTimKhongDauSuaVaXoaMem() throws Exception {
        long an = id(qt(post("/api/admin/san-pham"), "{\"ten\":\"Loa ẩn\",\"danhMucId\":%d,\"hienThi\":false}".formatted(danhMuc)));
        qt(get("/api/admin/san-pham").param("danhMucId", String.valueOf(danhMuc)), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noiDung[*].id", containsInAnyOrder((int) sp, (int) an)))
                .andExpect(jsonPath("$.kichThuoc").value(20));
        qt(get("/api/admin/san-pham").param("danhMucId", String.valueOf(danhMuc)).param("tuKhoa", "khong day"), null)
                .andExpect(jsonPath("$.noiDung[*].id", contains((int) sp)))
                .andExpect(jsonPath("$.noiDung[0].tenDanhMuc").value("Tai nghe"))
                .andExpect(jsonPath("$.noiDung[0].tongTon").value(0));
        qt(get("/api/admin/san-pham").param("danhMucId", String.valueOf(danhMuc)).param("hienThi", "false"), null)
                .andExpect(jsonPath("$.noiDung[*].id", contains((int) an)));

        qt(put("/api/admin/san-pham/" + sp), "{\"ten\":\"Tai nghe Y\",\"slug\":\"tai-nghe-y\",\"danhMucId\":%d,\"noiBat\":true}".formatted(danhMuc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("tai-nghe-y"))
                .andExpect(jsonPath("$.noiBat").value(true))
                .andExpect(jsonPath("$.thongSos", hasSize(0)))
                .andExpect(jsonPath("$.moTa").doesNotExist());
        qt(put("/api/admin/san-pham/" + sp), "{\"ten\":\"Z\",\"danhMucId\":999999999}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Danh mục không tồn tại"));

        qt(delete("/api/admin/san-pham/" + sp), null).andExpect(status().isNoContent());
        qt(get("/api/admin/san-pham/" + sp), null).andExpect(status().isNotFound());
        List<Integer> conLai = jdbc.queryForList("SELECT 1 FROM bien_the WHERE san_pham_id = ? AND deleted_at IS NULL", Integer.class, sp);
        assertThat(conLai).isEmpty();
        mvc.perform(post("/api/admin/san-pham").header("Authorization", khach).contentType("application/json")
                .content("{\"ten\":\"X\",\"danhMucId\":%d}".formatted(danhMuc))).andExpect(status().isForbidden());
    }

    @Test
    void doiNhanhHienThiGiuNguyenThongTinKhac() throws Exception {
        qt(patch("/api/admin/san-pham/" + sp + "/hien-thi"), "{\"hienThi\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hienThi").value(false))
                .andExpect(jsonPath("$.ten").value("Tai nghe Không Dây X"))
                .andExpect(jsonPath("$.thongSos", hasSize(2)));
        qt(get("/api/admin/san-pham").param("danhMucId", String.valueOf(danhMuc)).param("hienThi", "false"), null)
                .andExpect(jsonPath("$.noiDung[*].id", contains((int) sp)));
        qt(patch("/api/admin/san-pham/" + sp + "/hien-thi"), "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.hienThi").value("Vui lòng chọn hiển thị hoặc ẩn"));
        qt(patch("/api/admin/san-pham/999999999/hien-thi"), "{\"hienThi\":true}").andExpect(status().isNotFound());
        mvc.perform(patch("/api/admin/san-pham/" + sp + "/hien-thi").header("Authorization", khach)
                .contentType("application/json").content("{\"hienThi\":true}")).andExpect(status().isForbidden());
    }
}

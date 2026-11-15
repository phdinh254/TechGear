package com.techgear.danhgia;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DanhGiaIT extends KiemThuTichHop {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    TaoDuLieuSanPham tao;
    String token;
    long spA;
    long spB;
    long btA;
    long btB;

    @BeforeEach
    void chuanBi() throws Exception {
        tao = new TaoDuLieuSanPham(jdbc);
        long dm = tao.danhMuc("DM", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        spA = tao.sanPham("Tai nghe A", "tai-nghe-a-" + TaoDuLieuSanPham.hauTo(), dm, null);
        btA = tao.bienThe(spA, "A-" + TaoDuLieuSanPham.hauTo(), 100_000, null, 50);
        spB = tao.sanPham("Cáp B", "cap-b-" + TaoDuLieuSanPham.hauTo(), dm, null);
        btB = tao.bienThe(spB, "B-" + TaoDuLieuSanPham.hauTo(), 50_000, null, 50);
        token = dangKy("Trần Thị Bình");
    }

    String dangKy(String hoTen) throws Exception {
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"%s","email":"dg-%s@vi-du.vn","matKhau":"matkhau123"}
                        """.formatted(hoTen, TaoDuLieuSanPham.hauTo())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    /** Đặt đơn COD gồm các biến thể (mỗi cái 1), trả mã đơn. */
    String datHang(String tk, long... bienThes) throws Exception {
        StringBuilder dongs = new StringBuilder();
        for (long bt : bienThes) {
            dongs.append(dongs.isEmpty() ? "" : ",").append("{\"bienTheId\":%d,\"soLuong\":1}".formatted(bt));
        }
        String json = mvc.perform(post("/api/don-hang").header("Authorization", tk).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dongs":[%s],"tenNguoiNhan":"An","soDienThoai":"0912345678","tinhThanh":"HCM",
                                 "phuongXa":"Sài Gòn","diaChiChiTiet":"1 Lê Lợi","phuongThucThanhToan":"cod"}
                                """.formatted(dongs)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.maDonHang");
    }

    void hoanThanh(String maDon) {
        jdbc.update("UPDATE don_hang SET trang_thai = 'hoan_thanh' WHERE ma_don_hang = ?", maDon);
    }

    ResultActions vietDanhGia(String tk, String maDon, long sanPhamId, int soSao, String noiDung) throws Exception {
        return mvc.perform(post("/api/danh-gia").header("Authorization", tk).contentType(MediaType.APPLICATION_JSON)
                .content("{\"maDonHang\":\"%s\",\"sanPhamId\":%d,\"soSao\":%d%s}".formatted(maDon, sanPhamId, soSao,
                        noiDung == null ? "" : ",\"noiDung\":\"" + noiDung + "\"")));
    }

    @Test
    void chiVietDuocChoDonHoanThanhMoiSanPhamMotLan() throws Exception {
        String ma = datHang(token, btA, btB);
        mvc.perform(get("/api/danh-gia/co-the-viet").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        vietDanhGia(token, ma, spA, 5, "Tốt")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Chỉ đánh giá được đơn đã hoàn thành"));

        hoanThanh(ma);
        mvc.perform(get("/api/danh-gia/co-the-viet").header("Authorization", token))
                .andExpect(jsonPath("$[*].sanPhamId", containsInAnyOrder((int) spA, (int) spB)))
                .andExpect(jsonPath("$[0].maDonHang").value(ma))
                .andExpect(jsonPath("$[0].tenSanPham").exists())
                .andExpect(jsonPath("$[0].sanPhamSlug").exists())
                .andExpect(jsonPath("$[0].ngayDat").exists());

        vietDanhGia(token, ma.toLowerCase(), spA, 5, "  Âm thanh tốt  ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sanPhamId").value(spA))
                .andExpect(jsonPath("$.soSao").value(5))
                .andExpect(jsonPath("$.noiDung").value("Âm thanh tốt"))
                .andExpect(jsonPath("$.trangThai").value("cho_duyet"))
                .andExpect(jsonPath("$.ngayTao").exists());
        mvc.perform(get("/api/danh-gia/co-the-viet").header("Authorization", token))
                .andExpect(jsonPath("$[*].sanPhamId", contains((int) spB)));
        vietDanhGia(token, ma, spA, 4, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Bạn đã đánh giá sản phẩm này trong đơn hàng này"));
        vietDanhGia(token, ma, spB, 3, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.noiDung").doesNotExist());
    }

    @Test
    void sanPhamNgoaiDonDonNguoiKhacVaDuLieuSai() throws Exception {
        String ma = datHang(token, btA);
        hoanThanh(ma);
        String khac = dangKy("Người khác");

        vietDanhGia(token, ma, spB, 5, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Sản phẩm không có trong đơn hàng"));
        vietDanhGia(khac, ma, spA, 5, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy đơn hàng"));
        vietDanhGia(token, ma, spA, 6, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.soSao").value("Số sao từ 1 đến 5"));
        vietDanhGia(token, ma, spA, 5, "a".repeat(2001))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.noiDung").exists());
        mvc.perform(post("/api/danh-gia").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maDonHang\":\"%s\",\"sanPhamId\":%d,\"soSao\":5}".formatted(ma, spA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void congKhaiChiDanhGiaDaDuyetVaDiemTrungBinh() throws Exception {
        String khac = dangKy("Lê Văn Cường");
        String don1 = datHang(token, btA);
        String don2 = datHang(token, btA);
        String don3 = datHang(khac, btA);
        String don4 = datHang(khac, btA);
        for (String ma : new String[]{don1, don2, don3, don4}) {
            hoanThanh(ma);
        }
        vietDanhGia(token, don1, spA, 5, "Rất tốt").andExpect(status().isCreated());
        vietDanhGia(token, don2, spA, 4, null).andExpect(status().isCreated());
        vietDanhGia(khac, don3, spA, 4, "Ổn").andExpect(status().isCreated());
        vietDanhGia(khac, don4, spA, 1, "Chờ duyệt").andExpect(status().isCreated());
        jdbc.update("UPDATE danh_gia SET trang_thai = 'da_duyet' WHERE san_pham_id = ? AND so_sao > 1", spA);
        String slugA = tao.slugSanPham(spA);

        mvc.perform(get("/api/san-pham/" + slugA))
                .andExpect(jsonPath("$.soDanhGia").value(3))
                .andExpect(jsonPath("$.diemTrungBinh").value(4.3));
        mvc.perform(get("/api/san-pham/" + tao.slugSanPham(spB)))
                .andExpect(jsonPath("$.soDanhGia").value(0))
                .andExpect(jsonPath("$.diemTrungBinh").doesNotExist());
        mvc.perform(get("/api/san-pham/" + slugA + "/danh-gia").param("kichThuoc", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tongSoPhanTu").value(3))
                .andExpect(jsonPath("$.tongSoTrang").value(2))
                .andExpect(jsonPath("$.noiDung[*].soSao", contains(4, 4)))
                .andExpect(jsonPath("$.noiDung[0].hoTen").value("Lê Văn Cường"))
                .andExpect(jsonPath("$.noiDung[0].noiDung").value("Ổn"))
                .andExpect(jsonPath("$.noiDung[1].noiDung").doesNotExist())
                .andExpect(jsonPath("$.noiDung[0].ngayTao").exists());

        tao.sua("UPDATE san_pham SET hien_thi = FALSE WHERE id = ?", spA);
        mvc.perform(get("/api/san-pham/" + slugA + "/danh-gia"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy sản phẩm"));
    }
}

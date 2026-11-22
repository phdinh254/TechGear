package com.techgear.donhang;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Tiện ích chung cho kiểm thử đặt hàng: khách hàng đã đăng ký, danh mục, tạo mã giảm giá, gửi đơn. */
abstract class KiemThuDonHang extends KiemThuTichHop {

    static final String NGUOI_NHAN = """
            "tenNguoiNhan":"Nguyễn Văn An","soDienThoai":"0912345678","tinhThanh":"Thành phố Hồ Chí Minh",
            "phuongXa":"Phường Sài Gòn","diaChiChiTiet":"1 Lê Lợi\"""";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    TaoDuLieuSanPham tao;
    long danhMuc;
    String token;
    String email;

    @BeforeEach
    void chuanBiChung() throws Exception {
        tao = new TaoDuLieuSanPham(jdbc);
        danhMuc = tao.danhMuc("Phụ kiện", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        email = "dh-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn";
        token = dangKy(email);
    }

    String dangKy(String email) throws Exception {
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Khách đặt hàng","email":"%s","matKhau":"matkhau123"}
                        """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    /** Biến thể của một sản phẩm mới; trả id biến thể. */
    long bienThe(String ten, long gia, int ton) {
        long sp = tao.sanPham(ten, "sp-" + TaoDuLieuSanPham.hauTo(), danhMuc, null);
        return tao.bienThe(sp, "SKU-" + TaoDuLieuSanPham.hauTo(), gia, null, ton);
    }

    long maGiamGia(String ma, String loai, long giaTri, long donToiThieu, Long giamToiDa, Integer tongLuot,
                   Integer luotMoiNguoi) {
        return tao.them("""
                INSERT INTO ma_giam_gia (ma, loai_giam, gia_tri_giam, don_toi_thieu, giam_toi_da, tong_luot_dung,
                                         luot_dung_moi_nguoi, bat_dau_luc, ket_thuc_luc)
                VALUES (?, ?, ?, ?, ?, ?, ?, now() - INTERVAL 1 DAY, now() + INTERVAL 1 DAY)
                """, ma, loai, giaTri, donToiThieu, giamToiDa, tongLuot, luotMoiNguoi);
    }

    static String dong(long bienTheId, int soLuong) {
        return "{\"bienTheId\":%d,\"soLuong\":%d}".formatted(bienTheId, soLuong);
    }

    /** Thân yêu cầu đặt hàng COD; {@code them} là các trường thêm, bắt đầu bằng dấu phẩy. */
    static String don(String them, String... dongs) {
        return "{\"dongs\":[" + String.join(",", dongs) + "]," + NGUOI_NHAN
                + ",\"phuongThucThanhToan\":\"cod\"" + them + "}";
    }

    ResultActions datHang(String body) throws Exception {
        return mvc.perform(post("/api/don-hang").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    ResultActions datHangVangLai(String body) throws Exception {
        return mvc.perform(ipNgauNhien(post("/api/don-hang/vang-lai"))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    String maDon(ResultActions kq) throws Exception {
        return JsonPath.read(kq.andReturn().getResponse().getContentAsString(), "$.maDonHang");
    }

    int tonKho(long bienTheId) {
        return jdbc.queryForObject("SELECT so_luong_ton FROM bien_the WHERE id = ?", Integer.class, bienTheId);
    }

    /** Bộ đếm giới hạn số lần theo IP dùng chung cả bộ test: mỗi yêu cầu công khai dùng một IP riêng. */
    static MockHttpServletRequestBuilder ipNgauNhien(MockHttpServletRequestBuilder b) {
        String ip = "10.%d.%d.%d".formatted(ThreadLocalRandom.current().nextInt(256),
                ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256));
        return b.with(r -> {
            r.setRemoteAddr(ip);
            return r;
        });
    }
}

package com.techgear.thanhtoan;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import com.techgear.donhang.DonHangService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThanhToanVnPayIT extends KiemThuTichHop {

    static final String KHOA = "khoa-kiem-thu-vnpay";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    DonHangService donHangService;
    TaoDuLieuSanPham tao;
    String token;
    long bienThe;

    @BeforeEach
    void chuanBi() throws Exception {
        tao = new TaoDuLieuSanPham(jdbc);
        long dm = tao.danhMuc("DM", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        long sp = tao.sanPham("Tai nghe", "sp-" + TaoDuLieuSanPham.hauTo(), dm, null);
        bienThe = tao.bienThe(sp, "SKU-" + TaoDuLieuSanPham.hauTo(), 600_000, null, 10);
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Khách VNPay","email":"vnp-%s@vi-du.vn","matKhau":"matkhau123"}
                        """.formatted(TaoDuLieuSanPham.hauTo())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        token = "Bearer " + JsonPath.read(json, "$.token");
    }

    String datHang(String phuongThuc) throws Exception {
        String json = mvc.perform(post("/api/don-hang").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dongs":[{"bienTheId":%d,"soLuong":1}],"tenNguoiNhan":"An","soDienThoai":"0912345678",
                                 "tinhThanh":"HCM","phuongXa":"Sài Gòn","diaChiChiTiet":"1 Lê Lợi","phuongThucThanhToan":"%s"}
                                """.formatted(bienThe, phuongThuc)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.maDonHang");
    }

    /** Tạo URL thanh toán, trả các tham số vnp_* đã giải mã. */
    Map<String, String> taoUrl(String maDon) throws Exception {
        String json = mvc.perform(post("/api/thanh-toan/vnpay/" + maDon))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Map<String, String> ts = new LinkedHashMap<>();
        UriComponentsBuilder.fromUriString(JsonPath.read(json, "$.duongDan")).build().getQueryParams()
                .forEach((k, v) -> ts.put(k, URLDecoder.decode(v.getFirst(), StandardCharsets.UTF_8)));
        return ts;
    }

    /** Tham số IPN như VNPay gửi về cho một giao dịch, đã ký bằng {@code khoa}. */
    static Map<String, String> ipn(Map<String, String> url, String maPhanHoi, String khoa) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("vnp_TmnCode", url.get("vnp_TmnCode"));
        m.put("vnp_Amount", url.get("vnp_Amount"));
        m.put("vnp_BankCode", "NCB");
        m.put("vnp_BankTranNo", "VNP14512345");
        m.put("vnp_CardType", "ATM");
        m.put("vnp_OrderInfo", url.get("vnp_OrderInfo"));
        m.put("vnp_PayDate", "20261115103000");
        m.put("vnp_ResponseCode", maPhanHoi);
        m.put("vnp_TransactionNo", String.valueOf(System.nanoTime() % 100_000_000));
        m.put("vnp_TransactionStatus", maPhanHoi);
        m.put("vnp_TxnRef", url.get("vnp_TxnRef"));
        m.put("vnp_SecureHash", ChuKyVnPay.ky(m, khoa));
        return m;
    }

    ResultActions goi(String duongDan, Map<String, String> thamSo) throws Exception {
        MockHttpServletRequestBuilder b = get(duongDan);
        thamSo.forEach(b::param);
        return mvc.perform(b);
    }

    @Test
    void taoUrlDungThamSoVaChuKy() throws Exception {
        String ma = datHang("vnpay");

        Map<String, String> ts = taoUrl(ma);

        assertThat(ts).containsEntry("vnp_Version", "2.1.0").containsEntry("vnp_Command", "pay")
                .containsEntry("vnp_TmnCode", "TESTCODE").containsEntry("vnp_Amount", "60000000")
                .containsEntry("vnp_CurrCode", "VND").containsEntry("vnp_Locale", "vn")
                .containsEntry("vnp_OrderType", "other").containsEntry("vnp_TxnRef", ma + "01")
                .containsEntry("vnp_OrderInfo", "Thanh toan don hang " + ma)
                .containsEntry("vnp_ReturnUrl", "http://localhost:5173/thanh-toan/ket-qua")
                .containsKeys("vnp_CreateDate", "vnp_ExpireDate", "vnp_IpAddr");
        assertThat(ts.get("vnp_CreateDate")).matches("\\d{14}");
        assertThat(ChuKyVnPay.hopLe(ts, KHOA)).isTrue();
        assertThat(jdbc.queryForMap("SELECT so_tien, trang_thai FROM thanh_toan WHERE ma_tham_chieu = ?", ma + "01"))
                .containsEntry("so_tien", 600_000L).containsEntry("trang_thai", "cho_xu_ly");
        assertThat(taoUrl(ma)).containsEntry("vnp_TxnRef", ma + "02");
    }

    @Test
    void ipnThanhCongCapNhatDungMotLan() throws Exception {
        String ma = datHang("vnpay");
        Map<String, String> m = ipn(taoUrl(ma), "00", KHOA);

        goi("/api/thanh-toan/vnpay/ipn", m)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}", true));
        goi("/api/thanh-toan/vnpay/ipn", m)
                .andExpect(content().json("{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}", true));

        Map<String, Object> tt = jdbc.queryForMap(
                "SELECT trang_thai, ma_giao_dich, thanh_toan_luc, du_lieu_phan_hoi::text AS du_lieu FROM thanh_toan WHERE ma_tham_chieu = ?",
                ma + "01");
        assertThat(tt).containsEntry("trang_thai", "thanh_cong").containsEntry("ma_giao_dich", m.get("vnp_TransactionNo"));
        assertThat(tt.get("thanh_toan_luc")).isNotNull();
        assertThat((String) tt.get("du_lieu")).contains("\"vnp_BankCode\": \"NCB\"");
        mvc.perform(get("/api/don-hang/" + ma).header("Authorization", token))
                .andExpect(jsonPath("$.trangThaiThanhToan").value("da_thanh_toan"))
                .andExpect(jsonPath("$.trangThai").value("cho_xac_nhan"));
        mvc.perform(post("/api/thanh-toan/vnpay/" + ma))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đơn hàng đã được thanh toán"));
    }

    @Test
    void ipnSaiChuKyKhongCoGiaoDichSaiSoTien() throws Exception {
        String ma = datHang("vnpay");
        Map<String, String> url = taoUrl(ma);

        goi("/api/thanh-toan/vnpay/ipn", ipn(url, "00", "khoa-sai"))
                .andExpect(content().json("{\"RspCode\":\"97\",\"Message\":\"Invalid signature\"}", true));
        goi("/api/thanh-toan/vnpay/ipn", Map.of("vnp_TxnRef", ma + "01"))
                .andExpect(content().json("{\"RspCode\":\"97\"}"));

        Map<String, String> khongCo = new LinkedHashMap<>(url);
        khongCo.put("vnp_TxnRef", "DH000000XXXXXX01");
        goi("/api/thanh-toan/vnpay/ipn", ipn(khongCo, "00", KHOA))
                .andExpect(content().json("{\"RspCode\":\"01\",\"Message\":\"Order not found\"}", true));

        Map<String, String> saiTien = new LinkedHashMap<>(url);
        saiTien.put("vnp_Amount", "100");
        goi("/api/thanh-toan/vnpay/ipn", ipn(saiTien, "00", KHOA))
                .andExpect(content().json("{\"RspCode\":\"04\",\"Message\":\"Invalid amount\"}", true));

        assertThat(jdbc.queryForObject("SELECT trang_thai FROM thanh_toan WHERE ma_tham_chieu = ?", String.class, ma + "01"))
                .isEqualTo("cho_xu_ly");
        assertThat(jdbc.queryForObject("SELECT trang_thai_thanh_toan FROM don_hang WHERE ma_don_hang = ?", String.class, ma))
                .isEqualTo("chua_thanh_toan");
    }

    @Test
    void ipnThatBaiGhiNhanVaChoTaoLaiUrl() throws Exception {
        String ma = datHang("vnpay");
        goi("/api/thanh-toan/vnpay/ipn", ipn(taoUrl(ma), "24", KHOA))
                .andExpect(content().json("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}", true));

        assertThat(jdbc.queryForObject("SELECT trang_thai FROM thanh_toan WHERE ma_tham_chieu = ?", String.class, ma + "01"))
                .isEqualTo("that_bai");
        assertThat(jdbc.queryForObject("SELECT trang_thai_thanh_toan FROM don_hang WHERE ma_don_hang = ?", String.class, ma))
                .isEqualTo("chua_thanh_toan");
        assertThat(taoUrl(ma)).containsEntry("vnp_TxnRef", ma + "02");
    }

    @Test
    void ketQuaChiDocKhongCapNhat() throws Exception {
        String ma = datHang("vnpay");
        Map<String, String> m = ipn(taoUrl(ma), "00", KHOA);

        goi("/api/thanh-toan/vnpay/ket-qua", m)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hopLe").value(true))
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.maDonHang").value(ma))
                .andExpect(jsonPath("$.trangThaiThanhToan").value("chua_thanh_toan"))
                .andExpect(jsonPath("$.thongBao").value("Thanh toán thành công"));
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM thanh_toan WHERE ma_tham_chieu = ?", String.class, ma + "01"))
                .isEqualTo("cho_xu_ly");

        goi("/api/thanh-toan/vnpay/ket-qua", ipn(taoUrl(ma), "00", "khoa-sai"))
                .andExpect(jsonPath("$.hopLe").value(false))
                .andExpect(jsonPath("$.thanhCong").value(false))
                .andExpect(jsonPath("$.maDonHang").doesNotExist())
                .andExpect(jsonPath("$.thongBao").value("Dữ liệu trả về không hợp lệ"));
    }

    @Test
    void khongTaoUrlChoDonKhongHopLe() throws Exception {
        mvc.perform(post("/api/thanh-toan/vnpay/DH000000XXXXXX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy đơn hàng"));
        mvc.perform(post("/api/thanh-toan/vnpay/" + datHang("cod")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đơn hàng không thanh toán qua VNPay"));
        String daHuy = datHang("vnpay");
        mvc.perform(post("/api/don-hang/" + daHuy + "/huy").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(post("/api/thanh-toan/vnpay/" + daHuy))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đơn hàng không còn chờ thanh toán"));
        String quaHan = datHang("vnpay");
        jdbc.update("UPDATE don_hang SET created_at = now() - interval '31 minutes' WHERE ma_don_hang = ?", quaHan);
        mvc.perform(post("/api/thanh-toan/vnpay/" + quaHan))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đơn hàng đã quá hạn thanh toán"));
    }

    @Test
    void tuHuyDonVnPayChuaThanhToanQua35Phut() throws Exception {
        String quaHan = datHang("vnpay");
        String moiDat = datHang("vnpay");
        String cod = datHang("cod");
        String daTra = datHang("vnpay");
        goi("/api/thanh-toan/vnpay/ipn", ipn(taoUrl(daTra), "00", KHOA)).andExpect(content().json("{\"RspCode\":\"00\"}"));
        jdbc.update("UPDATE don_hang SET created_at = now() - interval '36 minutes' WHERE ma_don_hang IN (?, ?, ?)",
                quaHan, cod, daTra);
        int tonTruoc = jdbc.queryForObject("SELECT so_luong_ton FROM bien_the WHERE id = ?", Integer.class, bienThe);

        int soDon = donHangService.huyDonVnPayQuaHan();

        assertThat(soDon).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM don_hang WHERE ma_don_hang = ?", String.class, quaHan)).isEqualTo("da_huy");
        for (String con : new String[]{moiDat, cod, daTra}) {
            assertThat(jdbc.queryForObject("SELECT trang_thai FROM don_hang WHERE ma_don_hang = ?", String.class, con))
                    .isEqualTo("cho_xac_nhan");
        }
        assertThat(jdbc.queryForObject("SELECT so_luong_ton FROM bien_the WHERE id = ?", Integer.class, bienThe)).isEqualTo(tonTruoc + 1);
        mvc.perform(get("/api/don-hang/" + quaHan).header("Authorization", token))
                .andExpect(jsonPath("$.lichSu[1].ghiChu").value(startsWith("Quá 30 phút")));
    }
}

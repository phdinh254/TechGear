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
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThanhToanPayOsIT extends KiemThuTichHop {

    static final String KHOA = "khoa-kiem-thu-payos";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    DonHangService donHangService;
    @Autowired
    PayOsClientGia.Gia payOs;
    @Autowired
    JsonMapper jsonMapper;
    String token;
    long bienThe;

    @BeforeEach
    void chuanBi() throws Exception {
        payOs.datLai();
        TaoDuLieuSanPham tao = new TaoDuLieuSanPham(jdbc);
        long dm = tao.danhMuc("DM", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        long sp = tao.sanPham("Tai nghe", "sp-" + TaoDuLieuSanPham.hauTo(), dm, null);
        bienThe = tao.bienThe(sp, "SKU-" + TaoDuLieuSanPham.hauTo(), 600_000, null, 10);
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Khách PayOS","email":"payos-%s@vi-du.vn","matKhau":"matkhau123"}
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

    long idDon(String ma) {
        return jdbc.queryForObject("SELECT id FROM don_hang WHERE ma_don_hang = ?", Long.class, ma);
    }

    /** Tạo link, trả orderCode mà backend gửi PayOS. */
    long taoLink(String ma) throws Exception {
        mvc.perform(post("/api/thanh-toan/payos/" + ma))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duongDan").value(startsWith("https://pay.payos.vn/web/gia-")));
        return payOs.yeuCau.getLast().orderCode();
    }

    /** Body webhook như PayOS gửi, {@code data} ký bằng {@code khoa}. */
    String webhook(long orderCode, long soTien, String ma, String khoa) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderCode", orderCode);
        data.put("amount", soTien);
        data.put("description", "TG" + orderCode);
        data.put("accountNumber", "12345678");
        data.put("reference", "FT" + orderCode);
        data.put("transactionDateTime", "2026-11-23 10:30:00");
        data.put("currency", "VND");
        data.put("paymentLinkId", "gia-" + orderCode);
        data.put("code", ma);
        data.put("desc", "00".equals(ma) ? "Thành công" : "Thất bại");
        data.put("counterAccountBankId", "");
        data.put("counterAccountName", null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "00");
        body.put("desc", "success");
        body.put("success", true);
        body.put("data", data);
        body.put("signature", ChuKyPayOs.ky(ChuKyPayOs.chuoiDuLieu(data), khoa));
        return jsonMapper.writeValueAsString(body);
    }

    ResultActions guiWebhook(String body) throws Exception {
        return mvc.perform(post("/api/thanh-toan/payos/webhook").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    ResultActions ketQua(long orderCode, String ma) throws Exception {
        return mvc.perform(get("/api/thanh-toan/payos/ket-qua").param("orderCode", String.valueOf(orderCode)).param("ma", ma))
                .andExpect(status().isOk());
    }

    Map<String, Object> giaoDich(long orderCode) {
        return jdbc.queryForMap("SELECT trang_thai, phuong_thuc, so_tien, ma_giao_dich, thanh_toan_luc FROM thanh_toan WHERE ma_tham_chieu = ?",
                String.valueOf(orderCode));
    }

    String trangThaiThanhToan(String ma) {
        return jdbc.queryForObject("SELECT trang_thai_thanh_toan FROM don_hang WHERE ma_don_hang = ?", String.class, ma);
    }

    @Test
    void taoLink_traCheckoutUrlVaGhiGiaoDich() throws Exception {
        String ma = datHang("payos");

        long orderCode = taoLink(ma);

        PayOsClient.YeuCauLink yc = payOs.yeuCau.getLast();
        assertThat(orderCode).isEqualTo(idDon(ma) * 100 + 1);
        assertThat(yc.amount()).isEqualTo(600_000);
        assertThat(yc.description()).isEqualTo(("TG" + orderCode).substring(Math.max(0, ("TG" + orderCode).length() - 9)));
        assertThat(yc.description()).hasSizeLessThanOrEqualTo(9);
        assertThat(yc.returnUrl()).isEqualTo("http://localhost:5173/thanh-toan/ket-qua/" + ma);
        assertThat(yc.cancelUrl()).isEqualTo("http://localhost:5173/thanh-toan/ket-qua/" + ma);
        OffsetDateTime datLuc = jdbc.queryForObject("SELECT created_at FROM don_hang WHERE ma_don_hang = ?", OffsetDateTime.class, ma);
        assertThat(yc.expiredAt()).isEqualTo(datLuc.toEpochSecond() + 1800);
        assertThat(giaoDich(orderCode)).containsEntry("phuong_thuc", "payos").containsEntry("trang_thai", "cho_xu_ly")
                .containsEntry("so_tien", 600_000L);
    }

    @Test
    void taoLink_traThongTinTomTatChoTrangThanhToan() throws Exception {
        String ma = datHang("payos");

        String json = mvc.perform(post("/api/thanh-toan/payos/" + ma))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").value(idDon(ma) * 100 + 1))
                .andExpect(jsonPath("$.maDonHang").value(ma))
                .andExpect(jsonPath("$.tongTien").value(600_000))
                .andReturn().getResponse().getContentAsString();

        OffsetDateTime datLuc = jdbc.queryForObject("SELECT created_at FROM don_hang WHERE ma_don_hang = ?", OffsetDateTime.class, ma);
        String hetHan = JsonPath.read(json, "$.hetHan");
        assertThat(OffsetDateTime.parse(hetHan).toEpochSecond()).isEqualTo(datLuc.toEpochSecond() + 1800);
    }

    @Test
    void taoLinkLanHai_tangSoLanThu() throws Exception {
        String ma = datHang("payos");
        assertThat(taoLink(ma) % 100).isEqualTo(1);
        assertThat(taoLink(ma) % 100).isEqualTo(2);
    }

    @Test
    void khongTaoLinkChoDonKhongHopLe() throws Exception {
        mvc.perform(post("/api/thanh-toan/payos/DH000000XXXXXX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy đơn hàng"));
        mvc.perform(post("/api/thanh-toan/payos/" + datHang("cod")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đơn hàng không thanh toán qua PayOS"));
        String quaHan = datHang("payos");
        jdbc.update("UPDATE don_hang SET created_at = now() - INTERVAL 31 MINUTE WHERE ma_don_hang = ?", quaHan);
        mvc.perform(post("/api/thanh-toan/payos/" + quaHan))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đơn hàng đã quá hạn thanh toán"));
        String daTra = datHang("payos");
        guiWebhook(webhook(taoLink(daTra), 600_000, "00", KHOA));
        mvc.perform(post("/api/thanh-toan/payos/" + daTra))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đơn hàng đã được thanh toán"));
    }

    @Test
    void webhook_hopLe_danhDauDaThanhToan() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);

        guiWebhook(webhook(orderCode, 600_000, "00", KHOA));

        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "thanh_cong").containsEntry("ma_giao_dich", "FT" + orderCode);
        assertThat(giaoDich(orderCode).get("thanh_toan_luc")).isNotNull();
        assertThat(trangThaiThanhToan(ma)).isEqualTo("da_thanh_toan");
        assertThat(jdbc.queryForObject("SELECT CAST(du_lieu_phan_hoi AS CHAR) FROM thanh_toan WHERE ma_tham_chieu = ?",
                String.class, String.valueOf(orderCode))).contains("\"reference\": \"FT" + orderCode + "\"");
    }

    @Test
    void webhook_saiChuKyHoacSaiSoTien_khongGhi() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);

        guiWebhook(webhook(orderCode, 600_000, "00", "khoa-sai"));
        guiWebhook(webhook(orderCode, 100, "00", KHOA));

        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "cho_xu_ly");
        assertThat(trangThaiThanhToan(ma)).isEqualTo("chua_thanh_toan");
    }

    @Test
    void webhook_orderCodeLa_khongLoi() throws Exception {
        guiWebhook(webhook(123, 2000, "00", KHOA));
        guiWebhook("{\"code\":\"00\",\"desc\":\"success\",\"success\":true}");
    }

    @Test
    void webhook_lapLai_chiGhiMotLan() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);
        String body = webhook(orderCode, 600_000, "00", KHOA);

        guiWebhook(body);
        Object lucDau = giaoDich(orderCode).get("thanh_toan_luc");
        guiWebhook(body);

        assertThat(giaoDich(orderCode).get("thanh_toan_luc")).isEqualTo(lucDau);
        assertThat(jdbc.queryForObject("SELECT ghi_chu_quan_tri FROM don_hang WHERE ma_don_hang = ?", String.class, ma)).isNull();
    }

    @Test
    void webhook_thatBai_ghiThatBaiVaChoTaoLaiLink() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);

        guiWebhook(webhook(orderCode, 600_000, "01", KHOA));

        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "that_bai");
        assertThat(trangThaiThanhToan(ma)).isEqualTo("chua_thanh_toan");
        assertThat(taoLink(ma) % 100).isEqualTo(2);
    }

    @Test
    void ketQua_paid_ghiThanhCong_vaKhongHoiLai() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);
        payOs.trangThai = "PAID";

        for (int lan = 0; lan < 2; lan++) {
            ketQua(orderCode, ma)
                    .andExpect(jsonPath("$.hopLe").value(true))
                    .andExpect(jsonPath("$.thanhCong").value(true))
                    .andExpect(jsonPath("$.trangThai").value("thanh_cong"))
                    .andExpect(jsonPath("$.maDonHang").value(ma))
                    .andExpect(jsonPath("$.trangThaiThanhToan").value("da_thanh_toan"))
                    .andExpect(jsonPath("$.coTheThanhToanLai").value(false));
        }
        assertThat(payOs.soLanHoi.get()).isEqualTo(1);
        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "thanh_cong");
    }

    @Test
    void ketQua_pending_choXuLy() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);

        ketQua(orderCode, ma)
                .andExpect(jsonPath("$.hopLe").value(true))
                .andExpect(jsonPath("$.thanhCong").value(false))
                .andExpect(jsonPath("$.trangThai").value("cho_xu_ly"));
        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "cho_xu_ly");
    }

    @Test
    void ketQua_cancelled_ghiThatBai_vaChoThanhToanLai() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);
        payOs.trangThai = "CANCELLED";

        ketQua(orderCode, ma)
                .andExpect(jsonPath("$.hopLe").value(true))
                .andExpect(jsonPath("$.thanhCong").value(false))
                .andExpect(jsonPath("$.trangThai").value("that_bai"))
                .andExpect(jsonPath("$.maDonHang").value(ma))
                .andExpect(jsonPath("$.coTheThanhToanLai").value(true));
        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "that_bai");
    }

    @Test
    void ketQua_orderCodeKhongHopLe() throws Exception {
        for (String sai : new String[]{"abc", "999999", ""}) {
            mvc.perform(get("/api/thanh-toan/payos/ket-qua").param("orderCode", sai).param("ma", "DH000000XXXXXX"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hopLe").value(false))
                    .andExpect(jsonPath("$.thanhCong").value(false))
                    .andExpect(jsonPath("$.maDonHang").doesNotExist());
        }
    }

    @Test
    void ketQua_saiMaDon_khongLoThongTinVaKhongHoiPayOs() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);
        payOs.trangThai = "PAID";

        for (String sai : new String[]{"DH000000XXXXXX", ""}) {
            mvc.perform(get("/api/thanh-toan/payos/ket-qua").param("orderCode", String.valueOf(orderCode)).param("ma", sai))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hopLe").value(false))
                    .andExpect(jsonPath("$.maDonHang").doesNotExist());
        }
        mvc.perform(get("/api/thanh-toan/payos/ket-qua").param("orderCode", String.valueOf(orderCode)))
                .andExpect(jsonPath("$.hopLe").value(false));
        assertThat(payOs.soLanHoi.get()).isZero();
        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "cho_xu_ly");
        ketQua(orderCode, ma.toLowerCase()).andExpect(jsonPath("$.hopLe").value(true));
    }

    @Test
    void ketQua_payOsLoi_giuChoXuLy() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);
        payOs.loi = true;

        ketQua(orderCode, ma)
                .andExpect(jsonPath("$.hopLe").value(true))
                .andExpect(jsonPath("$.trangThai").value("cho_xu_ly"));
        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "cho_xu_ly");
    }

    @Test
    void webhook_referenceLaSo_vanGhiNhan() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);
        String body = webhook(orderCode, 600_000, "00", KHOA).replace("\"reference\":\"FT" + orderCode + "\"", "\"reference\":" + orderCode);
        // chữ ký phải tính trên dữ liệu thật gửi đi
        Map<String, Object> m = jsonMapper.readValue(body, Map.class);
        @SuppressWarnings("unchecked") Map<String, Object> data = (Map<String, Object>) m.get("data");
        m.put("signature", ChuKyPayOs.ky(ChuKyPayOs.chuoiDuLieu(data), KHOA));

        guiWebhook(jsonMapper.writeValueAsString(m));

        assertThat(giaoDich(orderCode)).containsEntry("trang_thai", "thanh_cong").containsEntry("ma_giao_dich", String.valueOf(orderCode));
    }

    @Test
    void traTienHaiLan_ghiChuCanHoanTien() throws Exception {
        String ma = datHang("payos");
        long lan1 = taoLink(ma);
        long lan2 = taoLink(ma);

        guiWebhook(webhook(lan1, 600_000, "00", KHOA));
        assertThat(jdbc.queryForObject("SELECT ghi_chu_quan_tri FROM don_hang WHERE ma_don_hang = ?", String.class, ma)).isNull();
        guiWebhook(webhook(lan2, 600_000, "00", KHOA));

        assertThat(giaoDich(lan2)).containsEntry("trang_thai", "thanh_cong");
        assertThat(jdbc.queryForObject("SELECT ghi_chu_quan_tri FROM don_hang WHERE ma_don_hang = ?", String.class, ma))
                .contains("lần nữa").contains(String.valueOf(lan2)).contains("cần hoàn tiền");
        assertThat(trangThaiThanhToan(ma)).isEqualTo("da_thanh_toan");
    }

    @Test
    void tienVeSauKhiDonTuHuy_giuHuyVaGhiChuHoanTien() throws Exception {
        String ma = datHang("payos");
        long orderCode = taoLink(ma);
        jdbc.update("UPDATE don_hang SET created_at = now() - INTERVAL 36 MINUTE WHERE ma_don_hang = ?", ma);
        assertThat(donHangService.huyDonPayOsQuaHan()).isEqualTo(1);

        guiWebhook(webhook(orderCode, 600_000, "00", KHOA));

        Map<String, Object> don = jdbc.queryForMap(
                "SELECT trang_thai, trang_thai_thanh_toan, ghi_chu_quan_tri FROM don_hang WHERE ma_don_hang = ?", ma);
        assertThat(don).containsEntry("trang_thai", "da_huy").containsEntry("trang_thai_thanh_toan", "da_thanh_toan");
        assertThat((String) don.get("ghi_chu_quan_tri")).contains("PayOS").contains("cần hoàn tiền");
        ketQua(orderCode, ma)
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.trangThaiDon").value("da_huy"));
        mvc.perform(get("/api/don-hang/" + ma).header("Authorization", token))
                .andExpect(jsonPath("$.lichSu[1].ghiChu").value(startsWith("Quá 30 phút chưa thanh toán PayOS")));
    }

    @Test
    void khachHuyDonDaThanhToanThiGhiChuCanHoanTien() throws Exception {
        String ma = datHang("payos");
        guiWebhook(webhook(taoLink(ma), 600_000, "00", KHOA));

        mvc.perform(post("/api/don-hang/" + ma + "/huy").header("Authorization", token)).andExpect(status().isOk());

        Map<String, Object> don = jdbc.queryForMap(
                "SELECT trang_thai, trang_thai_thanh_toan, ghi_chu_quan_tri FROM don_hang WHERE ma_don_hang = ?", ma);
        assertThat(don).containsEntry("trang_thai", "da_huy").containsEntry("trang_thai_thanh_toan", "da_thanh_toan");
        assertThat((String) don.get("ghi_chu_quan_tri")).contains("cần hoàn tiền");
    }

    @Test
    void tuHuyChiDonPayOsChuaThanhToanQua35Phut() throws Exception {
        String quaHan = datHang("payos");
        String moiDat = datHang("payos");
        String cod = datHang("cod");
        String daTra = datHang("payos");
        guiWebhook(webhook(taoLink(daTra), 600_000, "00", KHOA));
        jdbc.update("UPDATE don_hang SET created_at = now() - INTERVAL 36 MINUTE WHERE ma_don_hang IN (?, ?, ?)", quaHan, cod, daTra);

        assertThat(donHangService.huyDonPayOsQuaHan()).isEqualTo(1);

        assertThat(jdbc.queryForObject("SELECT trang_thai FROM don_hang WHERE ma_don_hang = ?", String.class, quaHan)).isEqualTo("da_huy");
        for (String con : new String[]{moiDat, cod, daTra}) {
            assertThat(jdbc.queryForObject("SELECT trang_thai FROM don_hang WHERE ma_don_hang = ?", String.class, con))
                    .isEqualTo("cho_xac_nhan");
        }
    }
}

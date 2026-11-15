package com.techgear.donhang;

import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DonHangCuaToiIT extends KiemThuDonHang {

    @Test
    void danhSachChiDonCuaMinhMoiNhatTruocLocTrangThai() throws Exception {
        long bt = bienThe("Tai nghe Z", 100_000, 20);
        String don1 = maDon(datHang(don("", dong(bt, 1))));
        String don2 = maDon(datHang(don("", dong(bt, 3))));
        jdbc.update("UPDATE don_hang SET trang_thai = 'da_xac_nhan' WHERE ma_don_hang = ?", don1);
        String khac = dangKy("khac-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn");
        mvc.perform(post("/api/don-hang").header("Authorization", khac).contentType(MediaType.APPLICATION_JSON)
                .content(don("", dong(bt, 1)))).andExpect(status().isCreated());

        mvc.perform(get("/api/don-hang").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noiDung[*].maDonHang", contains(don2, don1)))
                .andExpect(jsonPath("$.tongSoPhanTu").value(2))
                .andExpect(jsonPath("$.kichThuoc").value(10))
                .andExpect(jsonPath("$.noiDung[0].soLuongSanPham").value(3))
                .andExpect(jsonPath("$.noiDung[0].tenSanPhamDau").value("Tai nghe Z"))
                .andExpect(jsonPath("$.noiDung[0].tongTien").value(330_000))
                .andExpect(jsonPath("$.noiDung[0].trangThai").value("cho_xac_nhan"))
                .andExpect(jsonPath("$.noiDung[0].ngayDat").exists());
        mvc.perform(get("/api/don-hang").header("Authorization", token).param("trangThai", "da_xac_nhan"))
                .andExpect(jsonPath("$.noiDung[*].maDonHang", contains(don1)));
        mvc.perform(get("/api/don-hang").header("Authorization", token).param("trangThai", "lung_tung"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chiTietDonNguoiKhacLa404() throws Exception {
        long bt = bienThe("Cáp", 100_000, 5);
        String ma = maDon(datHang(don("", dong(bt, 1))));
        String khac = dangKy("khac-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn");

        mvc.perform(get("/api/don-hang/" + ma).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maDonHang").value(ma));
        mvc.perform(get("/api/don-hang/" + ma).header("Authorization", khac))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy đơn hàng"));
        mvc.perform(post("/api/don-hang/" + ma + "/huy").header("Authorization", khac))
                .andExpect(status().isNotFound());
    }

    @Test
    void huyDonHoanKhoTraLuotMaGhiLichSu() throws Exception {
        long bt = bienThe("Loa", 200_000, 5);
        String maGiam = "HUY" + TaoDuLieuSanPham.hauTo();
        long idMa = maGiamGia(maGiam, "so_tien", 20_000, 0, null, 1, 1);
        String ma = maDon(datHang(don(",\"maGiamGia\":\"" + maGiam + "\"", dong(bt, 2))));
        assertThat(tonKho(bt)).isEqualTo(3);

        mvc.perform(post("/api/don-hang/" + ma + "/huy").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"lyDo\":\"Đặt nhầm\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThai").value("da_huy"))
                .andExpect(jsonPath("$.coTheHuy").value(false))
                .andExpect(jsonPath("$.lichSu[*].trangThai", contains("cho_xac_nhan", "da_huy")))
                .andExpect(jsonPath("$.lichSu[1].ghiChu").value("Đặt nhầm"));

        assertThat(tonKho(bt)).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT da_dung FROM ma_giam_gia WHERE id = ?", Integer.class, idMa)).isZero();
        assertThat(jdbc.queryForObject("""
                SELECT k.so_luong_sau FROM lich_su_ton_kho k JOIN don_hang d ON d.id = k.don_hang_id
                WHERE d.ma_don_hang = ? AND k.loai = 'huy_don'
                """, Integer.class, ma)).isEqualTo(5);
        // mã chỉ 1 lượt tổng, 1 lượt mỗi người: đơn đã hủy trả lại lượt nên dùng lại được
        datHang(don(",\"maGiamGia\":\"" + maGiam + "\"", dong(bt, 1))).andExpect(status().isCreated());

        mvc.perform(post("/api/don-hang/" + ma + "/huy").header("Authorization", token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Chỉ hủy được đơn đang chờ xác nhận"));
    }

    @Test
    void khongHuyDonDaXacNhan() throws Exception {
        long bt = bienThe("Cáp", 100_000, 5);
        String ma = maDon(datHang(don("", dong(bt, 1))));
        jdbc.update("UPDATE don_hang SET trang_thai = 'da_xac_nhan' WHERE ma_don_hang = ?", ma);
        mvc.perform(post("/api/don-hang/" + ma + "/huy").header("Authorization", token))
                .andExpect(status().isConflict());
        assertThat(tonKho(bt)).isEqualTo(4);
    }

    @Test
    void traCuuDonVangLaiBangMaVaEmail() throws Exception {
        long bt = bienThe("Cáp", 100_000, 5);
        String emailKhach = "tracuu-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn";
        String ma = maDon(datHangVangLai(don(",\"email\":\"" + emailKhach + "\"", dong(bt, 1))));

        mvc.perform(ipNgauNhien(post("/api/don-hang/tra-cuu")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maDonHang\":\"%s\",\"email\":\"%s\"}".formatted(ma.toLowerCase(), emailKhach.toUpperCase())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maDonHang").value(ma))
                .andExpect(jsonPath("$.coTheHuy").value(false))
                .andExpect(jsonPath("$.dongs", hasSize(1)));
        mvc.perform(ipNgauNhien(post("/api/don-hang/tra-cuu")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maDonHang\":\"%s\",\"email\":\"sai@vi-du.vn\"}".formatted(ma)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy đơn hàng"));
    }

    @Test
    void traCuuSai10LanThiBiChan() throws Exception {
        String ip = "10.251.%d.1".formatted(java.util.concurrent.ThreadLocalRandom.current().nextInt(256));
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/don-hang/tra-cuu").with(r -> { r.setRemoteAddr(ip); return r; })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"maDonHang\":\"DH000000AAAAAA\",\"email\":\"a@vi-du.vn\"}"))
                    .andExpect(status().isNotFound());
        }
        mvc.perform(post("/api/don-hang/tra-cuu").with(r -> { r.setRemoteAddr(ip); return r; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maDonHang\":\"DH000000AAAAAA\",\"email\":\"a@vi-du.vn\"}"))
                .andExpect(status().isTooManyRequests());
    }
}

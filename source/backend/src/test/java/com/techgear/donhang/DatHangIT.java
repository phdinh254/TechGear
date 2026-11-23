package com.techgear.donhang;

import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DatHangIT extends KiemThuDonHang {

    ResultActions tinhThu(String body) throws Exception {
        return mvc.perform(post("/api/don-hang/tinh-thu").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    void themVaoGio(long bienTheId) throws Exception {
        mvc.perform(post("/api/gio-hang").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content(dong(bienTheId, 1))).andExpect(status().isOk());
    }

    @Test
    void datHangTinhTienLuuAnhChupTruKhoGhiLichSuXoaGio() throws Exception {
        String h = TaoDuLieuSanPham.hauTo();
        long sp = tao.sanPham("Tai nghe Y", "tai-nghe-y-" + h, danhMuc, null);
        long mau = tao.them(
                "INSERT INTO tuy_chon (san_pham_id, ten, loai) VALUES (?, 'Màu sắc', 'mau_sac')", sp);
        long den = tao.them(
                "INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri) VALUES (?, 'Đen')", mau);
        long bt1 = tao.bienThe(sp, "TNY-DEN-" + h, 200_000, 250_000L, 5);
        jdbc.update("INSERT INTO bien_the_gia_tri VALUES (?, ?)", bt1, den);
        jdbc.update("INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, la_anh_chinh) VALUES (?, '/uploads/san-pham/y.png', TRUE)", sp);
        long bt2 = bienThe("Cáp", 100_000, 10);
        long bt3 = bienThe("Không đặt", 50_000, 10);
        themVaoGio(bt1);
        themVaoGio(bt2);
        themVaoGio(bt3);

        ResultActions kq = datHang(don(",\"ghiChu\":\"Giao giờ hành chính\"", dong(bt1, 2), dong(bt2, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maDonHang").value(org.hamcrest.Matchers.matchesPattern("DH\\d{6}[A-Z2-9]{6}")))
                .andExpect(jsonPath("$.trangThai").value("cho_xac_nhan"))
                .andExpect(jsonPath("$.trangThaiThanhToan").value("chua_thanh_toan"))
                .andExpect(jsonPath("$.phuongThucThanhToan").value("cod"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.tenNguoiNhan").value("Nguyễn Văn An"))
                .andExpect(jsonPath("$.diaChiGiaoHang").value("1 Lê Lợi, Phường Sài Gòn, Thành phố Hồ Chí Minh"))
                .andExpect(jsonPath("$.ghiChu").value("Giao giờ hành chính"))
                .andExpect(jsonPath("$.tamTinh").value(500_000))
                .andExpect(jsonPath("$.tienGiamGia").value(0))
                .andExpect(jsonPath("$.phiVanChuyen").value(0))
                .andExpect(jsonPath("$.tongTien").value(500_000))
                .andExpect(jsonPath("$.maGiamGia").doesNotExist())
                .andExpect(jsonPath("$.coTheHuy").value(true))
                .andExpect(jsonPath("$.ngayDat").value(endsWith("+07:00")))
                .andExpect(jsonPath("$.dongs", hasSize(2)))
                .andExpect(jsonPath("$.dongs[0].bienTheId").value(bt1))
                .andExpect(jsonPath("$.dongs[0].sanPhamSlug").value("tai-nghe-y-" + h))
                .andExpect(jsonPath("$.dongs[0].tenSanPham").value("Tai nghe Y"))
                .andExpect(jsonPath("$.dongs[0].sku").value("TNY-DEN-" + h))
                .andExpect(jsonPath("$.dongs[0].phanLoai").value("Đen"))
                .andExpect(jsonPath("$.dongs[0].hinhAnh").value("/uploads/san-pham/y.png"))
                .andExpect(jsonPath("$.dongs[0].donGia").value(200_000))
                .andExpect(jsonPath("$.dongs[0].soLuong").value(2))
                .andExpect(jsonPath("$.dongs[0].thanhTien").value(400_000))
                .andExpect(jsonPath("$.lichSu", hasSize(1)))
                .andExpect(jsonPath("$.lichSu[0].trangThai").value("cho_xac_nhan"));
        String ma = maDon(kq);

        assertThat(tonKho(bt1)).isEqualTo(3);
        assertThat(tonKho(bt2)).isEqualTo(9);
        Map<String, Object> ls = jdbc.queryForMap("""
                SELECT k.loai, k.so_luong_thay_doi, k.so_luong_sau FROM lich_su_ton_kho k
                JOIN don_hang d ON d.id = k.don_hang_id WHERE d.ma_don_hang = ? AND k.bien_the_id = ?
                """, ma, bt1);
        assertThat(ls).containsEntry("loai", "ban_hang").containsEntry("so_luong_thay_doi", -2).containsEntry("so_luong_sau", 3);
        List<Long> conTrongGio = jdbc.queryForList("SELECT bien_the_id FROM gio_hang WHERE nguoi_dung_id = (SELECT id FROM nguoi_dung WHERE email = ?)", Long.class, email);
        assertThat(conTrongGio).containsExactly(bt3);
    }

    @Test
    void tinhThuKhongLuuKhongTruKhoVaTinhPhiVanChuyen() throws Exception {
        long bt = bienThe("Củ sạc", 100_000, 5);
        tinhThu("{\"dongs\":[" + dong(bt, 1) + "," + dong(bt, 2) + "]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dongs", hasSize(1)))
                .andExpect(jsonPath("$.dongs[0].soLuong").value(3))
                .andExpect(jsonPath("$.tamTinh").value(300_000))
                .andExpect(jsonPath("$.phiVanChuyen").value(30_000))
                .andExpect(jsonPath("$.tongTien").value(330_000));
        assertThat(tonKho(bt)).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM don_hang", Integer.class)).isZero();
    }

    @Test
    void maGiamGiaPhanTramMienPhiVanChuyenTinhSauGiam() throws Exception {
        long bt = bienThe("Loa", 520_000, 5);
        long id = maGiamGia("Giam10-" + TaoDuLieuSanPham.hauTo(), "phan_tram", 10, 0, null, null, null);
        String ma = jdbc.queryForObject("SELECT ma FROM ma_giam_gia WHERE id = ?", String.class, id);

        datHang(don(",\"maGiamGia\":\"" + ma.toLowerCase() + "\"", dong(bt, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maGiamGia").value(ma))
                .andExpect(jsonPath("$.tamTinh").value(520_000))
                .andExpect(jsonPath("$.tienGiamGia").value(52_000))
                .andExpect(jsonPath("$.phiVanChuyen").value(30_000))
                .andExpect(jsonPath("$.tongTien").value(498_000));
        assertThat(jdbc.queryForObject("SELECT da_dung FROM ma_giam_gia WHERE id = ?", Integer.class, id)).isEqualTo(1);
    }

    @Test
    void donPayOsTongTien0dPhaiChonCod() throws Exception {
        jdbc.update("UPDATE cai_dat SET gia_tri = '0' WHERE khoa = 'phi_van_chuyen'");
        long bt = bienThe("Cáp", 100_000, 5);
        long id = maGiamGia("Free-" + TaoDuLieuSanPham.hauTo(), "phan_tram", 100, 0, null, null, null);
        String ma = jdbc.queryForObject("SELECT ma FROM ma_giam_gia WHERE id = ?", String.class, id);

        datHang(don(",\"maGiamGia\":\"" + ma + "\"", dong(bt, 1)).replace("\"cod\"", "\"payos\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Đơn 0đ không cần thanh toán PayOS, vui lòng chọn thanh toán khi nhận hàng"));
        datHang(don(",\"maGiamGia\":\"" + ma + "\"", dong(bt, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tongTien").value(0));
    }

    @Test
    void maGiamGiaSaiTra400() throws Exception {
        long bt = bienThe("Loa", 100_000, 5);
        String h = TaoDuLieuSanPham.hauTo();
        maGiamGia("TOITHIEU" + h, "so_tien", 10_000, 500_000, null, null, null);
        long hetHan = maGiamGia("HETHAN" + h, "so_tien", 10_000, 0, null, null, null);
        jdbc.update("UPDATE ma_giam_gia SET bat_dau_luc = now() - INTERVAL 3 DAY, ket_thuc_luc = now() - INTERVAL 1 DAY WHERE id = ?", hetHan);

        tinhThu("{\"dongs\":[" + dong(bt, 1) + "],\"maGiamGia\":\"KHONGCO" + h + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Mã giảm giá không tồn tại"));
        tinhThu("{\"dongs\":[" + dong(bt, 1) + "],\"maGiamGia\":\"TOITHIEU" + h + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Đơn hàng tối thiểu 500.000đ để dùng mã này"));
        datHang(don(",\"maGiamGia\":\"HETHAN" + h + "\"", dong(bt, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Mã giảm giá đã hết hạn"));
        assertThat(tonKho(bt)).isEqualTo(5);
    }

    @Test
    void luotMoiNguoiTinhTheoTaiKhoanHoacEmail() throws Exception {
        long bt = bienThe("Cáp", 100_000, 10);
        String ma = "MOT" + TaoDuLieuSanPham.hauTo();
        maGiamGia(ma, "so_tien", 10_000, 0, null, null, 1);

        datHang(don(",\"maGiamGia\":\"" + ma + "\"", dong(bt, 1))).andExpect(status().isCreated());
        datHang(don(",\"maGiamGia\":\"" + ma + "\"", dong(bt, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Bạn đã dùng hết lượt của mã giảm giá này"));
        datHangVangLai(don(",\"email\":\"" + email.toUpperCase() + "\",\"maGiamGia\":\"" + ma + "\"", dong(bt, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Bạn đã dùng hết lượt của mã giảm giá này"));
        tinhThu("{\"dongs\":[" + dong(bt, 1) + "],\"maGiamGia\":\"" + ma + "\",\"email\":\"" + email + "\"}")
                .andExpect(status().isBadRequest());
        datHangVangLai(don(",\"email\":\"khac-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn\",\"maGiamGia\":\"" + ma + "\"", dong(bt, 1)))
                .andExpect(status().isCreated());
    }

    @Test
    void khongDuHangTra409NeuRoSanPhamVaKhongTruKhoDongKhac() throws Exception {
        long du = bienThe("Cáp", 100_000, 10);
        long thieu = bienThe("Loa thiếu hàng", 100_000, 2);
        long het = bienThe("Loa hết hàng", 100_000, 0);

        datHang(don("", dong(du, 1), dong(thieu, 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Sản phẩm Loa thiếu hàng chỉ còn 2 trong kho"));
        datHang(don("", dong(het, 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Sản phẩm Loa hết hàng đã hết hàng"));
        assertThat(tonKho(du)).isEqualTo(10);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM don_hang", Integer.class)).isZero();
    }

    @Test
    void bienTheKhongConBanLa404() throws Exception {
        long bt = bienThe("Đã tắt", 100_000, 5);
        tao.sua("UPDATE bien_the SET hien_thi = FALSE WHERE id = ?", bt);
        datHang(don("", dong(bt, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.thongBao").value("Phiên bản sản phẩm không còn bán"));
    }

    @Test
    void datHangVangLaiEmailChuThuongKhongGanTaiKhoanKhongCanToken() throws Exception {
        long bt = bienThe("Chuột", 300_000, 5);
        String emailKhach = "VangLai-" + TaoDuLieuSanPham.hauTo() + "@Vi-Du.vn";
        String ma = maDon(mvc.perform(ipNgauNhien(post("/api/don-hang/vang-lai"))
                        .header("Authorization", "Bearer token.da.het.han")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(don(",\"email\":\"" + emailKhach + "\",\"phuongThucThanhToan\":\"payos\"", dong(bt, 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(emailKhach.toLowerCase()))
                .andExpect(jsonPath("$.phuongThucThanhToan").value("payos"))
                .andExpect(jsonPath("$.coTheHuy").value(false)));
        assertThat(jdbc.queryForObject("SELECT nguoi_dung_id FROM don_hang WHERE ma_don_hang = ?", Long.class, ma)).isNull();

        datHangVangLai(don("", dong(bt, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Vui lòng nhập email để nhận thông tin đơn hàng"));
    }

    @Test
    void datHangVangLaiGioiHan10Don15PhutMoiIp() throws Exception {
        long bt = bienThe("Cáp", 1_000, 100);
        String body = don(",\"email\":\"gioihan-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn\"", dong(bt, 1));
        String ip = "10.250.%d.1".formatted(java.util.concurrent.ThreadLocalRandom.current().nextInt(256));
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/don-hang/vang-lai").with(r -> { r.setRemoteAddr(ip); return r; })
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        }
        mvc.perform(post("/api/don-hang/vang-lai").with(r -> { r.setRemoteAddr(ip); return r; })
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isTooManyRequests());
    }

    @Test
    void duLieuSaiVaChuaDangNhap() throws Exception {
        long bt = bienThe("Cáp", 100_000, 5);
        mvc.perform(post("/api/don-hang").contentType(MediaType.APPLICATION_JSON).content(don("", dong(bt, 1))))
                .andExpect(status().isUnauthorized());
        datHang(don(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.dongs").value("Chưa chọn sản phẩm nào"));
        datHang(don("", dong(bt, 1)).replace("0912345678", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.soDienThoai").exists());
        datHang(don("", dong(bt, 1)).replace("\"phuongThucThanhToan\":\"cod\"", "\"phuongThucThanhToan\":null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.phuongThucThanhToan").value("Vui lòng chọn phương thức thanh toán"));
    }
}

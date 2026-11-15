package com.techgear.donhang;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuQuanTri;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminDonHangIT extends KiemThuQuanTri {

    long sp;
    long bt;

    @BeforeEach
    void chuanBi() {
        long dm = tao.danhMuc("DM", "dm-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        sp = tao.sanPham("Chuột không dây", "chuot-" + TaoDuLieuSanPham.hauTo(), dm, null);
        bt = tao.bienThe(sp, "CH-" + TaoDuLieuSanPham.hauTo(), 200_000, null, 20);
    }

    String datHang(String token, String phuongThuc, String them) throws Exception {
        var b = post(token == null ? "/api/don-hang/vang-lai" : "/api/don-hang").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"dongs":[{"bienTheId":%d,"soLuong":2}],"tenNguoiNhan":"Phạm Minh","soDienThoai":"0987654321",
                         "tinhThanh":"Hà Nội","phuongXa":"Hoàn Kiếm","diaChiChiTiet":"2 Tràng Tiền","phuongThucThanhToan":"%s"%s}
                        """.formatted(bt, phuongThuc, them));
        if (token != null) {
            b.header("Authorization", token);
        } else {
            b.with(r -> { r.setRemoteAddr("10.9.%d.1".formatted((int) (Math.random() * 250))); return r; });
        }
        return JsonPath.read(mvc.perform(b).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(),
                "$.maDonHang");
    }

    ResultActions doiTrangThai(String ma, String trangThai) throws Exception {
        return qt(patch("/api/admin/don-hang/" + ma + "/trang-thai"), "{\"trangThai\":\"%s\",\"ghiChu\":\"QT %s\"}".formatted(trangThai, trangThai));
    }

    int tonKho() {
        return jdbc.queryForObject("SELECT so_luong_ton FROM bien_the WHERE id = ?", Integer.class, bt);
    }

    @Test
    void danhSachTimVaLoc() throws Exception {
        String don1 = datHang(khach, "cod", "");
        String don2 = datHang(null, "cod", ",\"email\":\"vanglai-" + TaoDuLieuSanPham.hauTo() + "@vi-du.vn\"");
        doiTrangThai(don1, "da_xac_nhan").andExpect(status().isOk());

        qt(get("/api/admin/don-hang").param("tuKhoa", "0987654321"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noiDung[*].maDonHang", contains(don2, don1)))
                .andExpect(jsonPath("$.noiDung[0].khachVangLai").value(true))
                .andExpect(jsonPath("$.noiDung[1].khachVangLai").value(false))
                .andExpect(jsonPath("$.noiDung[1].soLuongSanPham").value(2))
                .andExpect(jsonPath("$.kichThuoc").value(20));
        qt(get("/api/admin/don-hang").param("tuKhoa", don1.toLowerCase()), null)
                .andExpect(jsonPath("$.noiDung[*].maDonHang", contains(don1)));
        qt(get("/api/admin/don-hang").param("tuKhoa", "PHẠM MINH").param("trangThai", "cho_xac_nhan"), null)
                .andExpect(jsonPath("$.noiDung[*].maDonHang", contains(don2)));
        String homNay = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        qt(get("/api/admin/don-hang").param("tuKhoa", "0987654321").param("tuNgay", homNay).param("denNgay", homNay), null)
                .andExpect(jsonPath("$.tongSoPhanTu").value(2));
        qt(get("/api/admin/don-hang").param("tuKhoa", "0987654321").param("denNgay", "2020-01-01"), null)
                .andExpect(jsonPath("$.tongSoPhanTu").value(0));
        qt(get("/api/admin/don-hang").param("tuNgay", "2026-02-01").param("denNgay", "2026-01-01"), null)
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/don-hang").header("Authorization", khach)).andExpect(status().isForbidden());
    }

    @Test
    void chuyenTrangThaiDenHoanThanhCodTuDaThanhToanVaKhachDanhGiaDuoc() throws Exception {
        String ma = datHang(khach, "cod", "");
        doiTrangThai(ma, "hoan_thanh")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Không chuyển được đơn từ \"cho_xac_nhan\" sang \"hoan_thanh\""));
        doiTrangThai(ma, "da_xac_nhan").andExpect(status().isOk())
                .andExpect(jsonPath("$.trangThaiTiepTheo", containsInAnyOrder("dang_giao", "da_huy")));
        doiTrangThai(ma, "dang_giao").andExpect(jsonPath("$.trangThaiTiepTheo", contains("hoan_thanh")));
        doiTrangThai(ma, "hoan_thanh")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.don.trangThai").value("hoan_thanh"))
                .andExpect(jsonPath("$.don.trangThaiThanhToan").value("da_thanh_toan"))
                .andExpect(jsonPath("$.don.lichSu[*].trangThai", contains("cho_xac_nhan", "da_xac_nhan", "dang_giao", "hoan_thanh")))
                .andExpect(jsonPath("$.don.lichSu[3].ghiChu").value("QT hoan_thanh"))
                .andExpect(jsonPath("$.don.lichSu[3].nguoiThucHien").value("Người dùng thử"))
                .andExpect(jsonPath("$.trangThaiTiepTheo").isEmpty());
        mvc.perform(get("/api/don-hang/" + ma).header("Authorization", khach))
                .andExpect(jsonPath("$.lichSu[3].nguoiThucHien").doesNotExist());
        mvc.perform(post("/api/danh-gia").header("Authorization", khach).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maDonHang\":\"%s\",\"sanPhamId\":%d,\"soSao\":5}".formatted(ma, sp)))
                .andExpect(status().isCreated());
    }

    @Test
    void quanTriHuyDonDaXacNhanHoanKhoNhungKhongHuyDonDangGiao() throws Exception {
        String ma = datHang(khach, "cod", "");
        assertThat(tonKho()).isEqualTo(18);
        doiTrangThai(ma, "da_xac_nhan");
        doiTrangThai(ma, "da_huy")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.don.trangThai").value("da_huy"))
                .andExpect(jsonPath("$.don.lichSu[2].ghiChu").value("QT da_huy"));
        assertThat(tonKho()).isEqualTo(20);

        String dangGiao = datHang(khach, "cod", "");
        doiTrangThai(dangGiao, "da_xac_nhan");
        doiTrangThai(dangGiao, "dang_giao");
        doiTrangThai(dangGiao, "da_huy").andExpect(status().isConflict());
        qt(patch("/api/admin/don-hang/" + dangGiao + "/trang-thai"), "{\"trangThai\":\"lung_tung\"}").andExpect(status().isBadRequest());
        qt(patch("/api/admin/don-hang/DH000000XXXXXX/trang-thai"), "{\"trangThai\":\"da_huy\"}").andExpect(status().isNotFound());
    }

    @Test
    void donVnPayChuaTraKhongXacNhanDuocVaHoanTienSauKhiHuy() throws Exception {
        String ma = datHang(khach, "vnpay", "");
        doiTrangThai(ma, "da_xac_nhan")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Đơn VNPay chưa thanh toán, chưa xác nhận được"));
        qt(patch("/api/admin/don-hang/" + ma + "/thanh-toan"), "{\"trangThaiThanhToan\":\"da_thanh_toan\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Không cập nhật được trạng thái thanh toán"));

        jdbc.update("UPDATE don_hang SET trang_thai_thanh_toan = 'da_thanh_toan' WHERE ma_don_hang = ?", ma);
        qt(patch("/api/admin/don-hang/" + ma + "/thanh-toan"), "{\"trangThaiThanhToan\":\"da_hoan_tien\"}")
                .andExpect(status().isConflict());
        doiTrangThai(ma, "da_huy").andExpect(status().isOk());
        qt(patch("/api/admin/don-hang/" + ma + "/thanh-toan"), "{\"trangThaiThanhToan\":\"da_hoan_tien\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.don.trangThaiThanhToan").value("da_hoan_tien"));
    }

    @Test
    void codDanhDauDaThuTienVaGhiChuNoiBo() throws Exception {
        String ma = datHang(khach, "cod", "");
        qt(patch("/api/admin/don-hang/" + ma + "/thanh-toan"), "{\"trangThaiThanhToan\":\"da_thanh_toan\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.don.trangThaiThanhToan").value("da_thanh_toan"));
        qt(patch("/api/admin/don-hang/" + ma + "/ghi-chu"), "{\"ghiChuQuanTri\":\"Khách hẹn giao chiều\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ghiChuQuanTri").value("Khách hẹn giao chiều"))
                .andExpect(jsonPath("$.nguoiDungId").isNumber())
                .andExpect(jsonPath("$.thanhToans").isArray());
        qt(patch("/api/admin/don-hang/" + ma + "/ghi-chu"), "{\"ghiChuQuanTri\":\"\"}")
                .andExpect(jsonPath("$.ghiChuQuanTri").doesNotExist());
        mvc.perform(get("/api/don-hang/" + ma).header("Authorization", khach))
                .andExpect(jsonPath("$.ghiChuQuanTri").doesNotExist());
    }
}

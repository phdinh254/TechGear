package com.techgear.thongke;

import com.techgear.KiemThuQuanTri;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThongKeIT extends KiemThuQuanTri {

    long spA;
    long spB;

    /** Đơn đặt lúc {@code luc} (giờ VN) gồm (sản phẩm, số lượng, đơn giá) ; trả id đơn. */
    long don(String trangThai, String luc, long sp, int soLuong, long donGia) {
        long tong = soLuong * donGia;
        long id = jdbc.queryForObject("""
                INSERT INTO don_hang (ma_don_hang, ten_nguoi_nhan, email, so_dien_thoai, dia_chi_giao_hang, tam_tinh,
                                      tong_tien, phuong_thuc_thanh_toan, trang_thai, created_at)
                VALUES (?, 'A', 'a@vi-du.vn', '0912345678', 'HN', ?, ?, 'cod', ?, CAST(? AS timestamptz)) RETURNING id
                """, Long.class, "DH" + TaoDuLieuSanPham.hauTo().toUpperCase().substring(0, 8) + "TK", tong, tong, trangThai,
                luc + "+07");
        jdbc.update("""
                INSERT INTO chi_tiet_don_hang (don_hang_id, san_pham_id, ten_san_pham, sku, don_gia, so_luong, thanh_tien)
                VALUES (?, ?, (SELECT ten FROM san_pham WHERE id = ?), 'SKU', ?, ?, ?)
                """, id, sp, sp, donGia, soLuong, tong);
        return id;
    }

    @BeforeEach
    void chuanBi() {
        long dm = tao.danhMuc("TK", "tk-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
        spA = tao.sanPham("Sản phẩm A", "a-" + TaoDuLieuSanPham.hauTo(), dm, null);
        spB = tao.sanPham("Sản phẩm B", "b-" + TaoDuLieuSanPham.hauTo(), dm, null);
        // 23:30 ngày 1/10 giờ VN vẫn tính ngày 1/10 (UTC là 16:30)
        don("hoan_thanh", "2025-10-01 23:30:00", spA, 2, 100_000);
        don("hoan_thanh", "2025-10-03 08:00:00", spB, 1, 500_000);
        don("hoan_thanh", "2025-11-02 09:00:00", spA, 3, 100_000);
        don("da_huy", "2025-10-02 10:00:00", spB, 10, 500_000);
        don("cho_xac_nhan", "2025-10-02 11:00:00", spA, 1, 100_000);
    }

    @Test
    void doanhThuTheoNgayDuMocVaTheoThang() throws Exception {
        qt(get("/api/admin/thong-ke/doanh-thu").param("tu", "2025-10-01").param("den", "2025-10-03").param("theo", "ngay"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].moc", contains("2025-10-01", "2025-10-02", "2025-10-03")))
                .andExpect(jsonPath("$[*].doanhThu", contains(200_000, 0, 500_000)))
                .andExpect(jsonPath("$[*].soDon", contains(1, 0, 1)));
        qt(get("/api/admin/thong-ke/doanh-thu").param("tu", "2025-10-01").param("den", "2025-11-30").param("theo", "thang"), null)
                .andExpect(jsonPath("$[*].moc", contains("2025-10", "2025-11")))
                .andExpect(jsonPath("$[*].doanhThu", contains(700_000, 300_000)));
        qt(get("/api/admin/thong-ke/doanh-thu").param("tu", "2025-10-03").param("den", "2025-10-01").param("theo", "ngay"), null)
                .andExpect(status().isBadRequest());
        qt(get("/api/admin/thong-ke/doanh-thu").param("tu", "2024-01-01").param("den", "2025-12-31").param("theo", "ngay"), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Thống kê theo ngày tối đa 366 ngày"));
        qt(get("/api/admin/thong-ke/doanh-thu").param("tu", "2025-10-01"), null).andExpect(status().isBadRequest());
    }

    @Test
    void donTheoTrangThaiVaBanChay() throws Exception {
        qt(get("/api/admin/thong-ke/don-hang").param("tu", "2025-10-01").param("den", "2025-10-31"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tongDon").value(4))
                .andExpect(jsonPath("$.theoTrangThai.hoan_thanh").value(2))
                .andExpect(jsonPath("$.theoTrangThai.da_huy").value(1))
                .andExpect(jsonPath("$.theoTrangThai.cho_xac_nhan").value(1))
                .andExpect(jsonPath("$.theoTrangThai.dang_giao").value(0))
                .andExpect(jsonPath("$.doanhThu").value(700_000));

        qt(get("/api/admin/thong-ke/ban-chay").param("tu", "2025-10-01").param("den", "2025-11-30"), null)
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sanPhamId").value(spA))
                .andExpect(jsonPath("$[0].tenSanPham").value("Sản phẩm A"))
                .andExpect(jsonPath("$[0].soLuongBan").value(5))
                .andExpect(jsonPath("$[0].doanhThu").value(500_000))
                .andExpect(jsonPath("$[1].soLuongBan").value(1));
        qt(get("/api/admin/thong-ke/ban-chay").param("tu", "2025-10-01").param("den", "2025-11-30").param("gioiHan", "1"), null)
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/admin/thong-ke/ban-chay").header("Authorization", khach)).andExpect(status().isForbidden());
    }
}

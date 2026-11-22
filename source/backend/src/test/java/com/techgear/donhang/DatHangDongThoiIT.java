package com.techgear.donhang;

import com.jayway.jsonpath.JsonPath;
import com.techgear.KiemThuTichHop;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Đặt hàng đồng thời trên CSDL thật (không bọc giao dịch test để hai yêu cầu thật sự chạy song song):
 * hai người cùng mua món chỉ còn 1, hai đơn cùng dùng mã chỉ còn 1 lượt.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DatHangDongThoiIT extends KiemThuTichHop {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    TaoDuLieuSanPham tao;
    String hauTo;
    long danhMuc;

    @BeforeEach
    void chuanBi() {
        tao = new TaoDuLieuSanPham(jdbc);
        hauTo = TaoDuLieuSanPham.hauTo();
        danhMuc = tao.danhMuc("Đồng thời", "dong-thoi-" + hauTo, null, 0, true);
    }

    @AfterEach
    void donDep() {
        jdbc.update("DELETE FROM lich_su_ton_kho WHERE bien_the_id IN (SELECT bt.id FROM bien_the bt JOIN san_pham sp ON sp.id = bt.san_pham_id WHERE sp.danh_muc_id = ?)", danhMuc);
        jdbc.update("DELETE FROM don_hang WHERE email LIKE ?", "%-" + hauTo + "@vi-du.vn");
        jdbc.update("DELETE FROM ma_giam_gia WHERE ma LIKE ?", "%" + hauTo);
        jdbc.update("DELETE FROM nguoi_dung WHERE email LIKE ?", "%-" + hauTo + "@vi-du.vn");
        jdbc.update("DELETE FROM bien_the WHERE san_pham_id IN (SELECT id FROM san_pham WHERE danh_muc_id = ?)", danhMuc);
        jdbc.update("DELETE FROM san_pham WHERE danh_muc_id = ?", danhMuc);
        jdbc.update("DELETE FROM danh_muc WHERE id = ?", danhMuc);
    }

    String dangKy(String ten) throws Exception {
        String json = mvc.perform(post("/api/auth/dang-ky").contentType(MediaType.APPLICATION_JSON).content("""
                        {"hoTen":"Khách","email":"%s-%s@vi-du.vn","matKhau":"matkhau123"}
                        """.formatted(ten, hauTo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }

    /** Hai khách gửi cùng một đơn đúng cùng lúc; trả "mã HTTP thông báo lỗi" của hai yêu cầu. */
    List<String> haiNguoiDatCungLuc(String body) throws Exception {
        List<String> tokens = List.of(dangKy("a"), dangKy("b"));
        CountDownLatch xuatPhat = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<String>> kq = new ArrayList<>();
            for (String t : tokens) {
                Callable<String> viec = () -> {
                    xuatPhat.await();
                    var res = mvc.perform(post("/api/don-hang").header("Authorization", t)
                            .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse();
                    return res.getStatus() == 201 ? "201"
                            : res.getStatus() + " " + JsonPath.read(res.getContentAsString(), "$.thongBao");
                };
                kq.add(pool.submit(viec));
            }
            xuatPhat.countDown();
            List<String> ma = new ArrayList<>();
            for (Future<String> f : kq) {
                ma.add(f.get());
            }
            return ma;
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void haiNguoiMuaMonChiCon1ThiDungMotDonThanhCong() throws Exception {
        long sp = tao.sanPham("Món cuối", "mon-cuoi-" + hauTo, danhMuc, null);
        long bt = tao.bienThe(sp, "CUOI-" + hauTo, 100_000, null, 1);

        List<String> ma = haiNguoiDatCungLuc(KiemThuDonHang.don("", KiemThuDonHang.dong(bt, 1)));

        // thông báo "hết hàng" (không phải lỗi ràng buộc CSDL) chứng tỏ người thứ hai chờ khóa rồi đọc tồn kho mới
        assertThat(ma).containsExactlyInAnyOrder("201", "409 Sản phẩm Món cuối đã hết hàng");
        assertThat(jdbc.queryForObject("SELECT so_luong_ton FROM bien_the WHERE id = ?", Integer.class, bt)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM chi_tiet_don_hang WHERE bien_the_id = ?", Integer.class, bt)).isEqualTo(1);
    }

    @Test
    void haiDonCungDungMaChiCon1LuotThiMotDonBiTuChoi() throws Exception {
        long sp = tao.sanPham("Nhiều hàng", "nhieu-hang-" + hauTo, danhMuc, null);
        long bt = tao.bienThe(sp, "NHIEU-" + hauTo, 100_000, null, 10);
        jdbc.update("""
                INSERT INTO ma_giam_gia (ma, loai_giam, gia_tri_giam, tong_luot_dung, bat_dau_luc, ket_thuc_luc)
                VALUES (?, 'so_tien', 10000, 1, now() - INTERVAL 1 DAY, now() + INTERVAL 1 DAY)
                """, "MOTLUOT" + hauTo);

        List<String> ma = haiNguoiDatCungLuc(
                KiemThuDonHang.don(",\"maGiamGia\":\"MOTLUOT" + hauTo + "\"", KiemThuDonHang.dong(bt, 1)));

        assertThat(ma).containsExactlyInAnyOrder("201", "400 Mã giảm giá đã hết lượt sử dụng");
        assertThat(jdbc.queryForObject("SELECT da_dung FROM ma_giam_gia WHERE ma = ?", Integer.class, "MOTLUOT" + hauTo)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT so_luong_ton FROM bien_the WHERE id = ?", Integer.class, bt)).isEqualTo(9);
    }
}

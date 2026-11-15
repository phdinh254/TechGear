package com.techgear.sanpham;

import com.techgear.KiemThuQuanTri;
import com.techgear.TaoDuLieuSanPham;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminKhoIT extends KiemThuQuanTri {

    long danhMuc;

    @BeforeEach
    void chuanBi() {
        danhMuc = tao.danhMuc("Kho", "kho-" + TaoDuLieuSanPham.hauTo(), null, 0, true);
    }

    long bienThe(String ten, int ton) {
        long sp = tao.sanPham(ten, "sp-" + TaoDuLieuSanPham.hauTo(), danhMuc, null);
        return tao.bienThe(sp, "K-" + TaoDuLieuSanPham.hauTo(), 100_000, null, ton);
    }

    @Test
    void nhapKhoVaDieuChinhGhiLichSu() throws Exception {
        long bt = bienThe("Củ sạc", 5);

        qt(post("/api/admin/bien-the/" + bt + "/nhap-kho"), "{\"soLuong\":10,\"ghiChu\":\"Nhập lô 11/2026\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bienTheId").value(bt))
                .andExpect(jsonPath("$.soLuongTon").value(15));
        qt(post("/api/admin/bien-the/" + bt + "/dieu-chinh"), "{\"soLuongMoi\":12,\"ghiChu\":\"Kiểm kê thiếu 3\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.soLuongTon").value(12));
        qt(post("/api/admin/bien-the/" + bt + "/dieu-chinh"), "{\"soLuongMoi\":12,\"ghiChu\":\"Lại\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Số lượng mới bằng tồn kho hiện tại"));
        qt(post("/api/admin/bien-the/" + bt + "/dieu-chinh"), "{\"soLuongMoi\":1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.ghiChu").value("Vui lòng nhập lý do điều chỉnh"));
        qt(post("/api/admin/bien-the/" + bt + "/nhap-kho"), "{\"soLuong\":0}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.soLuong").exists());

        qt(get("/api/admin/kho/lich-su").param("bienTheId", String.valueOf(bt)), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noiDung[*].loai", contains("dieu_chinh", "nhap_kho")))
                .andExpect(jsonPath("$.noiDung[0].soLuongThayDoi").value(-3))
                .andExpect(jsonPath("$.noiDung[0].soLuongSau").value(12))
                .andExpect(jsonPath("$.noiDung[0].ghiChu").value("Kiểm kê thiếu 3"))
                .andExpect(jsonPath("$.noiDung[0].nguoiThucHien").value("Người dùng thử"))
                .andExpect(jsonPath("$.noiDung[0].tenSanPham").value("Củ sạc"))
                .andExpect(jsonPath("$.noiDung[1].soLuongThayDoi").value(10))
                .andExpect(jsonPath("$.noiDung[1].maDonHang").doesNotExist())
                .andExpect(jsonPath("$.kichThuoc").value(20));

        tao.sua("UPDATE bien_the SET deleted_at = now() WHERE id = ?", bt);
        qt(post("/api/admin/bien-the/" + bt + "/nhap-kho"), "{\"soLuong\":1}").andExpect(status().isNotFound());
    }

    @Test
    void lichSuCoMaDonKhiBanHangVaLocTheoLoai() throws Exception {
        long bt = bienThe("Loa", 5);
        String json = mvc.perform(post("/api/don-hang").header("Authorization", khach).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dongs":[{"bienTheId":%d,"soLuong":2}],"tenNguoiNhan":"An","soDienThoai":"0912345678",
                                 "tinhThanh":"HCM","phuongXa":"Sài Gòn","diaChiChiTiet":"1 Lê Lợi","phuongThucThanhToan":"cod"}
                                """.formatted(bt)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String maDon = com.jayway.jsonpath.JsonPath.read(json, "$.maDonHang");

        qt(get("/api/admin/kho/lich-su").param("bienTheId", String.valueOf(bt)).param("loai", "ban_hang"), null)
                .andExpect(jsonPath("$.tongSoPhanTu").value(1))
                .andExpect(jsonPath("$.noiDung[0].maDonHang").value(maDon))
                .andExpect(jsonPath("$.noiDung[0].soLuongThayDoi").value(-2));
        qt(get("/api/admin/kho/lich-su").param("loai", "lung_tung"), null).andExpect(status().isBadRequest());
    }

    @Test
    void sapHetGomBienTheTonDuoiNguongItHangTruoc() throws Exception {
        long it = bienThe("Ít", 1);
        long vua = bienThe("Vừa ngưỡng", 5);
        long nhieu = bienThe("Nhiều", 10);
        long daXoa = bienThe("Đã xóa", 0);
        tao.sua("UPDATE bien_the SET deleted_at = now() WHERE id = ?", daXoa);

        qt(get("/api/admin/kho/sap-het"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bienTheId == %d || @.bienTheId == %d)].bienTheId".formatted(it, vua),
                        contains((int) it, (int) vua)))
                .andExpect(jsonPath("$[*].bienTheId", not(hasItem((int) nhieu))))
                .andExpect(jsonPath("$[*].bienTheId", not(hasItem((int) daXoa))))
                .andExpect(jsonPath("$[?(@.bienTheId == %d)].nguongCanhBao".formatted(it)).value(5));
        mvc.perform(get("/api/admin/kho/sap-het").header("Authorization", khach)).andExpect(status().isForbidden());
    }
}

package com.techgear.service.admin;

import com.techgear.dto.admin.AdminSanPhamRequest;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.GiaTriTuyChon;
import com.techgear.entity.SanPham;
import com.techgear.entity.TuyChon;
import com.techgear.entity.enums.LoaiTuyChon;
import com.techgear.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BienTheDongBoTest {

    private final BienTheDongBo dongBo = new BienTheDongBo();

    // ---------- helper dựng dữ liệu ----------

    /** Màu(1): Đen(11), Trắng(12); biến thể Đen(101, TG-A-DEN, tồn 7), Trắng(102, TG-A-TRANG, tồn 0). */
    private static SanPham sanPhamMau() {
        SanPham sp = new SanPham();
        sp.setId(1L);
        TuyChon mau = tuyChon(sp, 1L, "Màu", 0);
        GiaTriTuyChon den = giaTri(mau, 11L, "Đen", 0);
        GiaTriTuyChon trang = giaTri(mau, 12L, "Trắng", 1);
        sp.getTuyChons().add(mau);
        sp.getBienThes().add(bienThe(sp, 101L, "TG-A-DEN", 7, den));
        sp.getBienThes().add(bienThe(sp, 102L, "TG-A-TRANG", 0, trang));
        return sp;
    }

    private static SanPham sanPhamMacDinh() {
        SanPham sp = new SanPham();
        sp.setId(2L);
        sp.getBienThes().add(bienThe(sp, 201L, "TG-B", 3));
        return sp;
    }

    private static TuyChon tuyChon(SanPham sp, Long id, String ten, int thuTu) {
        TuyChon tc = new TuyChon();
        tc.setId(id);
        tc.setSanPham(sp);
        tc.setTen(ten);
        tc.setLoai(LoaiTuyChon.mau_sac);
        tc.setThuTu(thuTu);
        return tc;
    }

    private static GiaTriTuyChon giaTri(TuyChon tc, Long id, String ten, int thuTu) {
        GiaTriTuyChon gt = new GiaTriTuyChon();
        gt.setId(id);
        gt.setTuyChon(tc);
        gt.setGiaTri(ten);
        gt.setThuTu(thuTu);
        tc.getGiaTris().add(gt);
        return gt;
    }

    private static BienTheSanPham bienThe(SanPham sp, Long id, String sku, int ton, GiaTriTuyChon... gts) {
        BienTheSanPham bt = new BienTheSanPham();
        bt.setId(id);
        bt.setSanPham(sp);
        bt.setSku(sku);
        bt.setSoLuongTon(ton);
        bt.getGiaTris().addAll(List.of(gts));
        return bt;
    }

    private static AdminSanPhamRequest.GiaTri gt(Long id, String ten) {
        return new AdminSanPhamRequest.GiaTri(id, ten, null);
    }

    private static AdminSanPhamRequest.TuyChon tc(Long id, String ten, AdminSanPhamRequest.GiaTri... gts) {
        return new AdminSanPhamRequest.TuyChon(id, ten, LoaiTuyChon.mau_sac, List.of(gts));
    }

    private static AdminSanPhamRequest.BienThe bt(String sku, BigDecimal giaBan, Integer ton, String... giaTris) {
        return new AdminSanPhamRequest.BienThe(List.of(giaTris), sku, giaBan, null, ton, 5, true);
    }

    private static AdminSanPhamRequest req(List<AdminSanPhamRequest.TuyChon> tuyChons,
                                           List<AdminSanPhamRequest.BienThe> bienThes) {
        return new AdminSanPhamRequest("Sạc dự phòng", 1L, null, null, null, new BigDecimal("100000"), null,
                12, false, true, List.of(), List.of(), tuyChons, bienThes);
    }

    private static BienTheSanPham timTheoId(SanPham sp, long id) {
        return sp.getBienThes().stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }

    private void kiemTraLoi(AdminSanPhamRequest r, HttpStatus status, String thongBao) {
        ApiException ex = assertThrows(ApiException.class, () -> dongBo.kiemTra(r));
        assertEquals(status, ex.getStatus());
        assertEquals(thongBao, ex.getMessage());
    }

    // ---------- dongBo ----------

    @Test
    void giuNguyenIdVaTonKhoKhiToHopConTonTai() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(11L, "Đen"), gt(12L, "Trắng"))),
                List.of(bt("TG-A-DEN", new BigDecimal("150000"), null, "Đen"),
                        bt("TG-A-TRANG", null, null, "Trắng")));
        dongBo.kiemTra(r);

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        BienTheSanPham den = timTheoId(sp, 101L);
        assertEquals(new BigDecimal("150000"), den.getGiaBan());
        assertEquals(7, den.getSoLuongTon());
        assertTrue(kq.moi().isEmpty());
        assertTrue(kq.biXoa().isEmpty());
        assertEquals(2, sp.getBienThes().size());
    }

    @Test
    void themGiaTriTaoBienTheMoi() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(11L, "Đen"), gt(12L, "Trắng"), gt(null, "Xanh"))),
                List.of(bt("TG-A-DEN", null, null, "Đen"),
                        bt("TG-A-TRANG", null, null, "Trắng"),
                        bt("tg-a-xanh", null, 5, "Xanh")));
        dongBo.kiemTra(r);

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals(1, kq.moi().size());
        BienTheSanPham moi = kq.moi().get(0);
        assertEquals(5, moi.getSoLuongTon());
        assertEquals("TG-A-XANH", moi.getSku());
        assertSame(sp, moi.getSanPham());
        GiaTriTuyChon xanh = sp.getTuyChons().get(0).getGiaTris().get(2);
        assertEquals("Xanh", xanh.getGiaTri());
        assertTrue(moi.getGiaTris().contains(xanh));
        assertFalse(sp.getBienThes().contains(moi));
        assertTrue(kq.biXoa().isEmpty());
    }

    @Test
    void boGiaTriXoaMemBienThe() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(11L, "Đen"))),
                List.of(bt("TG-A-DEN", null, null, "Đen")));
        dongBo.kiemTra(r);

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals(1, kq.biXoa().size());
        assertEquals(102L, kq.biXoa().get(0).getId());
        assertEquals(null, timTheoId(sp, 102L));
        assertTrue(sp.getTuyChons().get(0).getGiaTris().stream().noneMatch(g -> g.getGiaTri().equals("Trắng")));
        assertTrue(kq.moi().isEmpty());
    }

    @Test
    void skuCuaBienTheBiXoaDuocGiaiPhong() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(11L, "Đen"))),
                List.of(bt("TG-A-DEN", null, null, "Đen")));

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals("TG-A-TRANG#102", kq.biXoa().get(0).getSku());
    }

    @Test
    void doiTenGiaTriGiuBienThe() {
        SanPham sp = sanPhamMau();
        GiaTriTuyChon den = sp.getTuyChons().get(0).getGiaTris().get(0);
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(11L, "Đen nhám"), gt(12L, "Trắng"))),
                List.of(bt("TG-A-DEN", null, null, "Đen nhám"),
                        bt("TG-A-TRANG", null, null, "Trắng")));
        dongBo.kiemTra(r);

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals("Đen nhám", den.getGiaTri());
        assertSame(den, sp.getTuyChons().get(0).getGiaTris().get(0));
        assertTrue(timTheoId(sp, 101L).getGiaTris().contains(den));
        assertTrue(kq.moi().isEmpty());
        assertTrue(kq.biXoa().isEmpty());
    }

    @Test
    void doiTenGiaTriTrungTenGiaTriBiBoVanGiuDungBienThe() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(11L, "Trắng"))),
                List.of(bt("TG-A-TRANG-MOI", null, null, "Trắng")));
        dongBo.kiemTra(r);

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals(7, timTheoId(sp, 101L).getSoLuongTon());
        assertEquals(List.of(102L), kq.biXoa().stream().map(BienTheSanPham::getId).toList());
        assertTrue(kq.moi().isEmpty());
        assertEquals(List.of(11L), sp.getTuyChons().get(0).getGiaTris().stream().map(GiaTriTuyChon::getId).toList());
    }

    @Test
    void doiTenTuyChonGiuBienThe() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu sắc", gt(11L, "Đen"), gt(12L, "Trắng"))),
                List.of(bt("TG-A-DEN", null, null, "Đen"),
                        bt("TG-A-TRANG", null, null, "Trắng")));

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals("Màu sắc", sp.getTuyChons().get(0).getTen());
        assertTrue(kq.moi().isEmpty());
        assertTrue(kq.biXoa().isEmpty());
    }

    @Test
    void giaTriKhongIdTrungTenDungLaiEntityCu() {
        SanPham sp = sanPhamMau();
        GiaTriTuyChon trang = sp.getTuyChons().get(0).getGiaTris().get(1);
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(11L, "Đen"), gt(null, "Trắng"))),
                List.of(bt("TG-A-DEN", null, null, "Đen"),
                        bt("TG-A-TRANG", null, null, "Trắng")));

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertSame(trang, sp.getTuyChons().get(0).getGiaTris().get(1));
        assertEquals(12L, sp.getTuyChons().get(0).getGiaTris().get(1).getId());
        assertTrue(kq.moi().isEmpty());
        assertTrue(kq.biXoa().isEmpty());
    }

    @Test
    void themTuyChonThuHaiTaoLaiToanBoBienThe() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(11L, "Đen")), tc(null, "Dung lượng", gt(null, "10000mAh"))),
                List.of(bt("TG-A-DEN-10K", null, null, "Đen", "10000mAh")));
        dongBo.kiemTra(r);

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals(2, sp.getTuyChons().size());
        assertEquals(1, sp.getTuyChons().get(1).getThuTu());
        assertEquals(1, kq.moi().size());
        assertEquals(2, kq.moi().get(0).getGiaTris().size());
        assertEquals(2, kq.biXoa().size());
    }

    @Test
    void tuyChonIdKhongThuocSanPhamBiTuChoi() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(99L, "Màu", gt(null, "Đen"))),
                List.of(bt("TG-A-DEN", null, null, "Đen")));
        ApiException ex = assertThrows(ApiException.class, () -> dongBo.dongBo(sp, r));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Tùy chọn không thuộc sản phẩm", ex.getMessage());
    }

    @Test
    void giaTriIdKhongThuocTuyChonBiTuChoi() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(
                List.of(tc(1L, "Màu", gt(99L, "Đen"))),
                List.of(bt("TG-A-DEN", null, null, "Đen")));
        ApiException ex = assertThrows(ApiException.class, () -> dongBo.dongBo(sp, r));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Giá trị không thuộc tùy chọn", ex.getMessage());
    }

    @Test
    void chuyenTuMacDinhSangCoTuyChon() {
        SanPham sp = sanPhamMacDinh();
        AdminSanPhamRequest r = req(
                List.of(tc(null, "Màu", gt(null, "Đen"), gt(null, "Trắng"))),
                List.of(bt("TG-B-DEN", null, null, "Đen"),
                        bt("TG-B-TRANG", null, null, "Trắng")));
        dongBo.kiemTra(r);

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals(1, kq.biXoa().size());
        assertEquals(201L, kq.biXoa().get(0).getId());
        assertEquals(2, kq.moi().size());
        assertTrue(sp.getBienThes().isEmpty());
        assertEquals(1, sp.getTuyChons().size());
    }

    @Test
    void chuyenTuCoTuyChonVeMacDinh() {
        SanPham sp = sanPhamMau();
        AdminSanPhamRequest r = req(List.of(), List.of(bt("TG-A", null, 4)));
        dongBo.kiemTra(r);

        BienTheDongBo.KetQua kq = dongBo.dongBo(sp, r);

        assertEquals(List.of(101L, 102L), kq.biXoa().stream().map(BienTheSanPham::getId).toList());
        assertEquals(1, kq.moi().size());
        assertTrue(kq.moi().get(0).getGiaTris().isEmpty());
        assertEquals(4, kq.moi().get(0).getSoLuongTon());
        assertTrue(sp.getTuyChons().isEmpty());
    }

    @Test
    void khoaTheoThuTuTuyChon() {
        assertEquals("", BienTheDongBo.khoa(List.of()));
        assertEquals("", BienTheDongBo.khoa(sanPhamMacDinh().getBienThes().get(0)));
        assertEquals("Màu=Đen", BienTheDongBo.khoa(sanPhamMau().getBienThes().get(0)));
    }

    // ---------- kiemTra ----------

    @Test
    void kiemTraThieuToHopBiTuChoi() {
        AdminSanPhamRequest r = req(
                List.of(tc(null, "Màu", gt(null, "Đen"), gt(null, "Trắng")),
                        tc(null, "Dung lượng", gt(null, "10000mAh"), gt(null, "20000mAh"))),
                List.of(bt("A1", null, null, "Đen", "10000mAh"),
                        bt("A2", null, null, "Đen", "20000mAh"),
                        bt("A3", null, null, "Trắng", "10000mAh")));
        kiemTraLoi(r, HttpStatus.BAD_REQUEST, "Danh sách biến thể phải gồm đủ 4 tổ hợp");
    }

    @Test
    void kiemTraToHopTrungBiTuChoi() {
        AdminSanPhamRequest r = req(
                List.of(tc(null, "Màu", gt(null, "Đen"), gt(null, "Trắng"))),
                List.of(bt("A1", null, null, "Đen"),
                        bt("A2", null, null, "Đen")));
        kiemTraLoi(r, HttpStatus.BAD_REQUEST, "Có tổ hợp biến thể bị trùng");
    }

    @Test
    void kiemTraVuot50ToHopBiTuChoi() {
        AdminSanPhamRequest r = req(
                List.of(tc(null, "A", gt(null, "1"), gt(null, "2"), gt(null, "3"), gt(null, "4")),
                        tc(null, "B", gt(null, "1"), gt(null, "2"), gt(null, "3"), gt(null, "4")),
                        tc(null, "C", gt(null, "1"), gt(null, "2"), gt(null, "3"), gt(null, "4"))),
                List.of(bt("A1", null, null, "1", "1", "1")));
        kiemTraLoi(r, HttpStatus.BAD_REQUEST, "Tối đa 50 biến thể");
    }

    @Test
    void kiemTraSkuTrungTrongRequest() {
        AdminSanPhamRequest r = req(
                List.of(tc(null, "Màu", gt(null, "Đen"), gt(null, "Trắng"))),
                List.of(bt("tg-a", null, null, "Đen"),
                        bt("TG-A", null, null, "Trắng")));
        kiemTraLoi(r, HttpStatus.CONFLICT, "SKU TG-A bị trùng");
    }

    @Test
    void kiemTraGiaTriKhongThuocTuyChon() {
        AdminSanPhamRequest r = req(
                List.of(tc(null, "Màu", gt(null, "Đen"), gt(null, "Trắng"))),
                List.of(bt("A1", null, null, "Đen"),
                        bt("A2", null, null, "Xám")));
        kiemTraLoi(r, HttpStatus.BAD_REQUEST, "Giá trị \"Xám\" không thuộc tùy chọn \"Màu\"");
    }

    @Test
    void kiemTraTenTuyChonTrung() {
        AdminSanPhamRequest r = req(
                List.of(tc(null, "Màu", gt(null, "Đen")), tc(null, " màu ", gt(null, "Trắng"))),
                List.of(bt("A1", null, null, "Đen", "Trắng")));
        kiemTraLoi(r, HttpStatus.BAD_REQUEST, "Tên tùy chọn bị trùng: Màu");
    }

    @Test
    void kiemTraGiaTriTrongTuyChonTrung() {
        AdminSanPhamRequest r = req(
                List.of(tc(null, "Màu", gt(null, "Đen"), gt(null, " đen"))),
                List.of(bt("A1", null, null, "Đen")));
        ApiException ex = assertThrows(ApiException.class, () -> dongBo.kiemTra(r));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void kiemTraKhongTuyChonPhaiCoDungMotBienThe() {
        AdminSanPhamRequest r = req(List.of(),
                List.of(bt("A1", null, null), bt("A2", null, null)));
        kiemTraLoi(r, HttpStatus.BAD_REQUEST, "Sản phẩm không có tùy chọn phải có đúng 1 biến thể");
    }

    @Test
    void kiemTraHopLeKhongNem() {
        AdminSanPhamRequest r = req(
                List.of(tc(null, "Màu", gt(null, "Đen"), gt(null, "Trắng")),
                        tc(null, "Dung lượng", gt(null, "10000mAh"))),
                new ArrayList<>(List.of(bt("A1", null, null, "Trắng", "10000mAh"),
                        bt("A2", null, null, "đen", "10000mAh"))));
        dongBo.kiemTra(r);
        dongBo.kiemTra(req(null, List.of(bt("A", null, null))));
    }
}

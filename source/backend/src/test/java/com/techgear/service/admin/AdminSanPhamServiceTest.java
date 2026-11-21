package com.techgear.service.admin;

import com.techgear.dto.admin.AdminSanPhamRequest;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.DanhMuc;
import com.techgear.entity.GiaTriTuyChon;
import com.techgear.entity.LichSuTonKho;
import com.techgear.entity.SanPham;
import com.techgear.entity.TuyChon;
import com.techgear.entity.enums.LoaiBienDongKho;
import com.techgear.entity.enums.LoaiTuyChon;
import com.techgear.exception.ApiException;
import com.techgear.repository.BienTheSanPhamRepository;
import com.techgear.repository.ChiTietGioHangRepository;
import com.techgear.repository.DanhMucRepository;
import com.techgear.repository.LichSuTonKhoRepository;
import com.techgear.repository.NguoiDungRepository;
import com.techgear.repository.SanPhamRepository;
import com.techgear.repository.ThuongHieuRepository;
import com.techgear.repository.YeuThichRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSanPhamServiceTest {

    @Mock
    SanPhamRepository sanPhamRepository;
    @Mock
    BienTheSanPhamRepository bienTheSanPhamRepository;
    @Mock
    DanhMucRepository danhMucRepository;
    @Mock
    ThuongHieuRepository thuongHieuRepository;
    @Mock
    ChiTietGioHangRepository chiTietGioHangRepository;
    @Mock
    YeuThichRepository yeuThichRepository;
    @Mock
    LichSuTonKhoRepository lichSuTonKhoRepository;
    @Mock
    NguoiDungRepository nguoiDungRepository;

    AdminSanPhamService service;

    @BeforeEach
    void setUp() {
        service = new AdminSanPhamService(sanPhamRepository, bienTheSanPhamRepository, danhMucRepository,
                thuongHieuRepository, chiTietGioHangRepository, yeuThichRepository, lichSuTonKhoRepository,
                nguoiDungRepository, new BienTheDongBo());
    }

    // ---------- helper dựng dữ liệu ----------

    /** Màu(1): Đen(11), Trắng(12); biến thể Đen(101, TG-A-DEN, tồn 7), Trắng(102, TG-A-TRANG, tồn 0). */
    private static SanPham sanPhamMau() {
        SanPham sp = new SanPham();
        sp.setId(1L);
        sp.setTen("Sạc dự phòng");
        sp.setSlug("sac-du-phong");
        sp.setGiaBan(new BigDecimal("100000"));
        TuyChon mau = new TuyChon();
        mau.setId(1L);
        mau.setSanPham(sp);
        mau.setTen("Màu");
        mau.setLoai(LoaiTuyChon.mau_sac);
        GiaTriTuyChon den = giaTri(mau, 11L, "Đen", 0);
        GiaTriTuyChon trang = giaTri(mau, 12L, "Trắng", 1);
        sp.getTuyChons().add(mau);
        sp.getBienThes().add(bienThe(sp, 101L, "TG-A-DEN", 7, den));
        sp.getBienThes().add(bienThe(sp, 102L, "TG-A-TRANG", 0, trang));
        return sp;
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

    private static DanhMuc danhMuc() {
        DanhMuc dm = new DanhMuc();
        dm.setId(1L);
        dm.setTen("Sạc");
        return dm;
    }

    private static final List<AdminSanPhamRequest.HinhAnh> MOT_ANH =
            List.of(new AdminSanPhamRequest.HinhAnh("/uploads/a.jpg", null, true, null, null));

    private static AdminSanPhamRequest req(BigDecimal giaGoc, Boolean hienThi, List<AdminSanPhamRequest.HinhAnh> anhs,
                                           List<AdminSanPhamRequest.TuyChon> tuyChons,
                                           List<AdminSanPhamRequest.BienThe> bienThes) {
        return new AdminSanPhamRequest("Sạc dự phòng", 1L, null, null, null, new BigDecimal("100000"), giaGoc,
                12, false, hienThi, anhs, List.of(), tuyChons, bienThes);
    }

    private static AdminSanPhamRequest.BienThe macDinh(String sku, Integer ton) {
        return new AdminSanPhamRequest.BienThe(List.of(), sku, null, null, ton, 5, true);
    }

    private static void kiemTraLoi(HttpStatus status, String thongBao, Executable viec) {
        ApiException ex = assertThrows(ApiException.class, viec);
        assertEquals(status, ex.getStatus());
        assertEquals(thongBao, ex.getMessage());
    }

    // ---------- tests ----------

    @Test
    void taoSanPhamGhiLichSuKhoiTao() {
        when(danhMucRepository.findById(1L)).thenReturn(Optional.of(danhMuc()));
        AdminSanPhamRequest r = req(null, true, MOT_ANH, List.of(), List.of(macDinh("TG-NEW", 10)));

        service.tao(r, 5L);

        ArgumentCaptor<LichSuTonKho> captor = ArgumentCaptor.forClass(LichSuTonKho.class);
        verify(lichSuTonKhoRepository).save(captor.capture());
        LichSuTonKho ls = captor.getValue();
        assertEquals(LoaiBienDongKho.khoi_tao, ls.getLoai());
        assertEquals(10, ls.getSoLuongThayDoi());
        assertEquals(10, ls.getSoLuongSau());
        assertEquals("Khởi tạo biến thể", ls.getGhiChu());
        assertEquals("TG-NEW", ls.getBienThe().getSku());
        verify(sanPhamRepository).save(any(SanPham.class));
    }

    @Test
    void giaGocNhoHonGiaBanBiTuChoi() {
        AdminSanPhamRequest r = req(new BigDecimal("90000"), true, MOT_ANH, List.of(), List.of(macDinh("TG-NEW", 0)));

        kiemTraLoi(HttpStatus.BAD_REQUEST, "Giá gốc phải lớn hơn hoặc bằng giá bán", () -> service.tao(r, 5L));
        verify(sanPhamRepository, never()).flush();
    }

    @Test
    void sanPhamHienThiKhongAnhBiTuChoi() {
        AdminSanPhamRequest r = req(null, true, List.of(), List.of(), List.of(macDinh("TG-NEW", 0)));

        kiemTraLoi(HttpStatus.BAD_REQUEST, "Sản phẩm đang hiển thị phải có ít nhất 1 ảnh", () -> service.tao(r, 5L));
    }

    @Test
    void batHienThiSanPhamKhongAnhBiTuChoi() {
        SanPham sp = sanPhamMau();
        sp.setHienThi(false);
        when(sanPhamRepository.findById(1L)).thenReturn(Optional.of(sp));

        kiemTraLoi(HttpStatus.BAD_REQUEST, "Sản phẩm đang hiển thị phải có ít nhất 1 ảnh",
                () -> service.doiHienThi(1L, true));
        assertEquals(false, sp.getHienThi());
    }

    @Test
    void skuDangDungOSanPhamKhacBiTuChoi() {
        when(sanPhamRepository.findById(1L)).thenReturn(Optional.of(sanPhamMau()));
        when(danhMucRepository.findById(1L)).thenReturn(Optional.of(danhMuc()));
        when(bienTheSanPhamRepository.timSanPhamIdTheoSkuKeCaDaXoa("TG-X")).thenReturn(List.of(99L));
        AdminSanPhamRequest r = req(null, true, MOT_ANH, List.of(), List.of(macDinh("TG-X", 0)));

        kiemTraLoi(HttpStatus.CONFLICT, "SKU TG-X đã được dùng cho sản phẩm khác", () -> service.capNhat(1L, r, 5L));
        verify(sanPhamRepository, never()).flush();
    }

    @Test
    void xoaBienTheGoKhoiGioHang() {
        when(sanPhamRepository.findById(1L)).thenReturn(Optional.of(sanPhamMau()));
        when(danhMucRepository.findById(1L)).thenReturn(Optional.of(danhMuc()));
        AdminSanPhamRequest r = req(null, true, MOT_ANH,
                List.of(new AdminSanPhamRequest.TuyChon(1L, "Màu", LoaiTuyChon.mau_sac,
                        List.of(new AdminSanPhamRequest.GiaTri(11L, "Đen", null)))),
                List.of(new AdminSanPhamRequest.BienThe(List.of("Đen"), "TG-A-DEN", null, null, null, 5, true)));

        service.capNhat(1L, r, 5L);

        verify(chiTietGioHangRepository).xoaTheoBienThe(List.of(102L));
        verify(lichSuTonKhoRepository, never()).save(any());
        // Sản phẩm đã có: chỉ flush, không save (merge sẽ sao chép tùy chọn / giá trị mới thành instance khác)
        verify(sanPhamRepository, never()).save(any(SanPham.class));
        verify(sanPhamRepository, times(2)).flush();
    }

    @Test
    void xoaSanPhamGoGioHangVaYeuThich() {
        when(sanPhamRepository.findById(1L)).thenReturn(Optional.of(sanPhamMau()));

        service.xoa(1L);

        InOrder thuTu = inOrder(chiTietGioHangRepository, yeuThichRepository, bienTheSanPhamRepository,
                sanPhamRepository);
        thuTu.verify(chiTietGioHangRepository).xoaTheoSanPham(1L);
        thuTu.verify(yeuThichRepository).xoaTheoSanPham(1L);
        thuTu.verify(bienTheSanPhamRepository).xoaMemTheoSanPham(1L);
        thuTu.verify(sanPhamRepository).xoaMem(1L);
        verify(sanPhamRepository, never()).delete(any(SanPham.class));
    }
}

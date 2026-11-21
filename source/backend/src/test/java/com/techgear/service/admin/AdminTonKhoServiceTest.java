package com.techgear.service.admin;

import com.techgear.dto.admin.DieuChinhKhoRequest;
import com.techgear.dto.admin.AdminTonKhoResponse;
import com.techgear.dto.admin.NhapKhoRequest;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.LichSuTonKho;
import com.techgear.entity.NguoiDung;
import com.techgear.entity.SanPham;
import com.techgear.entity.enums.LoaiBienDongKho;
import com.techgear.exception.ApiException;
import com.techgear.repository.BienTheSanPhamRepository;
import com.techgear.repository.LichSuTonKhoRepository;
import com.techgear.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTonKhoServiceTest {

    @Mock
    BienTheSanPhamRepository bienTheSanPhamRepository;
    @Mock
    LichSuTonKhoRepository lichSuTonKhoRepository;
    @Mock
    NguoiDungRepository nguoiDungRepository;

    AdminTonKhoService service;
    BienTheSanPham bienThe;

    @BeforeEach
    void setUp() {
        service = new AdminTonKhoService(bienTheSanPhamRepository, lichSuTonKhoRepository, nguoiDungRepository);
        SanPham sp = new SanPham();
        sp.setId(1L);
        sp.setTen("Tai nghe");
        bienThe = new BienTheSanPham();
        bienThe.setId(7L);
        bienThe.setSanPham(sp);
        bienThe.setSku("TN-1");
        bienThe.setSoLuongTon(3);
        bienThe.setNguongCanhBao(5);
    }

    private LichSuTonKho daGhi() {
        ArgumentCaptor<LichSuTonKho> c = ArgumentCaptor.forClass(LichSuTonKho.class);
        verify(lichSuTonKhoRepository).save(c.capture());
        return c.getValue();
    }

    @Test
    void nhapKhoCongTonVaGhiLichSu() {
        when(bienTheSanPhamRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(bienThe));
        when(nguoiDungRepository.getReferenceById(9L)).thenReturn(new NguoiDung());

        AdminTonKhoResponse r = service.nhapKho(7L, new NhapKhoRequest(10, "Lô 11/2026"), 9L);

        assertEquals(13, bienThe.getSoLuongTon());
        assertEquals(13, r.soLuongTon());
        LichSuTonKho ls = daGhi();
        assertEquals(LoaiBienDongKho.nhap_kho, ls.getLoai());
        assertEquals(10, ls.getSoLuongThayDoi());
        assertEquals(13, ls.getSoLuongSau());
        assertEquals("Lô 11/2026", ls.getGhiChu());
    }

    @Test
    void dieuChinhGhiChenhLech() {
        bienThe.setSoLuongTon(13);
        when(bienTheSanPhamRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(bienThe));
        when(nguoiDungRepository.getReferenceById(9L)).thenReturn(new NguoiDung());

        service.dieuChinh(7L, new DieuChinhKhoRequest(9, "Kiểm kê"), 9L);

        assertEquals(9, bienThe.getSoLuongTon());
        LichSuTonKho ls = daGhi();
        assertEquals(LoaiBienDongKho.dieu_chinh, ls.getLoai());
        assertEquals(-4, ls.getSoLuongThayDoi());
        assertEquals(9, ls.getSoLuongSau());
        assertEquals("Kiểm kê", ls.getGhiChu());
    }

    @Test
    void dieuChinhKhongChenhLechBiTuChoi() {
        bienThe.setSoLuongTon(9);
        when(bienTheSanPhamRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(bienThe));

        ApiException ex = assertThrows(ApiException.class,
                () -> service.dieuChinh(7L, new DieuChinhKhoRequest(9, "Kiểm kê"), 9L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Số lượng thực tế bằng tồn kho hiện tại, không cần điều chỉnh", ex.getMessage());
        verify(lichSuTonKhoRepository, never()).save(any());
    }

    @Test
    void trangThaiTheoNguong() {
        assertEquals("het_hang", AdminTonKhoService.trangThai(bt(0, 5)));
        assertEquals("sap_het", AdminTonKhoService.trangThai(bt(5, 5)));
        assertEquals("con_hang", AdminTonKhoService.trangThai(bt(6, 5)));
    }

    @Test
    void bienTheKhongTonTaiTra404() {
        when(bienTheSanPhamRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> service.nhapKho(99L, new NhapKhoRequest(1, null), 9L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Không tìm thấy biến thể sản phẩm", ex.getMessage());
    }

    private static BienTheSanPham bt(int ton, int nguong) {
        BienTheSanPham b = new BienTheSanPham();
        b.setSoLuongTon(ton);
        b.setNguongCanhBao(nguong);
        return b;
    }
}

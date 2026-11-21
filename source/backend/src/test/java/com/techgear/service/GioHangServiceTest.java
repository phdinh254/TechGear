package com.techgear.service;

import com.techgear.dto.GioHangItemRequest;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.DanhMuc;
import com.techgear.entity.GioHang;
import com.techgear.entity.SanPham;
import com.techgear.exception.ApiException;
import com.techgear.repository.BienTheSanPhamRepository;
import com.techgear.repository.ChiTietGioHangRepository;
import com.techgear.repository.GioHangRepository;
import com.techgear.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GioHangServiceTest {

    @Mock
    private GioHangRepository gioHangRepository;

    @Mock
    private ChiTietGioHangRepository chiTietRepository;

    @Mock
    private BienTheSanPhamRepository bienTheRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    private GioHangService gioHangService;

    @BeforeEach
    void setUp() {
        gioHangService = new GioHangService(gioHangRepository, chiTietRepository, bienTheRepository,
                nguoiDungRepository);
    }

    @Test
    void themVaoGioSanPhamDanhMucAnBiChan() {
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setHienThi(false);
        SanPham sanPham = new SanPham();
        sanPham.setHienThi(true);
        sanPham.setDanhMuc(danhMuc);
        BienTheSanPham bienThe = new BienTheSanPham();
        bienThe.setId(5L);
        bienThe.setSanPham(sanPham);
        bienThe.setSoLuongTon(10);

        when(gioHangRepository.findByNguoiDungId(1L)).thenReturn(Optional.of(new GioHang()));
        when(bienTheRepository.findById(5L)).thenReturn(Optional.of(bienThe));

        ApiException loi = assertThrows(ApiException.class,
                () -> gioHangService.them(1L, new GioHangItemRequest(5L, 1)));

        assertEquals(HttpStatus.NOT_FOUND, loi.getStatus());
        assertEquals("Sản phẩm không còn được bán", loi.getMessage());
        verifyNoInteractions(chiTietRepository);
    }
}

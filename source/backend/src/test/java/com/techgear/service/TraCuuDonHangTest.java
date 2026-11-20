package com.techgear.service;

import com.techgear.dto.DonHangResponse;
import com.techgear.entity.DonHang;
import com.techgear.entity.enums.TrangThaiDonHang;
import com.techgear.exception.ApiException;
import com.techgear.repository.BienTheSanPhamRepository;
import com.techgear.repository.DonHangRepository;
import com.techgear.repository.LichSuTonKhoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraCuuDonHangTest {

    private static final String MA = "DH20261120120000ABCD";

    @Mock
    private DonHangRepository donHangRepository;
    @Mock
    private BienTheSanPhamRepository bienTheRepository;
    @Mock
    private LichSuTonKhoRepository lichSuTonKhoRepository;

    private DonHangKhachHangService service;

    @BeforeEach
    void setUp() {
        service = new DonHangKhachHangService(donHangRepository, bienTheRepository, lichSuTonKhoRepository);
    }

    private DonHang donHangMau() {
        DonHang donHang = new DonHang();
        donHang.setId(1L);
        donHang.setMaDonHang(MA);
        donHang.setEmail("Khach@Example.com");
        donHang.setSoDienThoai("0912 345 678");
        donHang.setTenNguoiNhan("Khách");
        donHang.setDiaChiGiaoHang("Hà Nội");
        donHang.setTamTinh(new BigDecimal("100000"));
        donHang.setTongTien(new BigDecimal("130000"));
        donHang.setTrangThai(TrangThaiDonHang.cho_xac_nhan);
        return donHang;
    }

    @Test
    void timThayKhiKhopEmailKhongPhanBietHoaThuong() {
        when(donHangRepository.findByMaDonHang(MA)).thenReturn(Optional.of(donHangMau()));
        DonHangResponse ketQua = service.traCuu(" " + MA.toLowerCase() + " ", "khach@example.com");
        assertEquals(MA, ketQua.maDonHang());
    }

    @Test
    void timThayKhiKhopSoDienThoaiDaChuanHoa() {
        when(donHangRepository.findByMaDonHang(MA)).thenReturn(Optional.of(donHangMau()));
        assertEquals(MA, service.traCuu(MA, "+84 912.345.678").maDonHang());
    }

    @Test
    void baoKhongTimThayKhiSaiThongTinLienHe() {
        when(donHangRepository.findByMaDonHang(MA)).thenReturn(Optional.of(donHangMau()));
        ApiException loi = assertThrows(ApiException.class, () -> service.traCuu(MA, "0999999999"));
        assertEquals(HttpStatus.NOT_FOUND, loi.getStatus());
    }

    @Test
    void saiMaVaSaiLienHeTraCungThongBao() {
        when(donHangRepository.findByMaDonHang(MA)).thenReturn(Optional.of(donHangMau()));
        when(donHangRepository.findByMaDonHang("DHKHONGCO")).thenReturn(Optional.empty());
        String saiLienHe = assertThrows(ApiException.class, () -> service.traCuu(MA, "a@b.vn")).getMessage();
        String saiMa = assertThrows(ApiException.class, () -> service.traCuu("DHKHONGCO", "a@b.vn")).getMessage();
        assertEquals(saiLienHe, saiMa);
    }

    @Test
    void chuanHoaSoDienThoai() {
        assertEquals("0912345678", DonHangKhachHangService.chuanHoaSoDienThoai("+84 912-345-678"));
        assertEquals("0912345678", DonHangKhachHangService.chuanHoaSoDienThoai("0912.345.678"));
        assertEquals("", DonHangKhachHangService.chuanHoaSoDienThoai("abc"));
    }

    @Test
    void chuoiKhongCoSoKhongKhopVoiDonNao() {
        assertFalse(DonHangKhachHangService.khopLienHe(donHangMau(), "---"));
    }
}

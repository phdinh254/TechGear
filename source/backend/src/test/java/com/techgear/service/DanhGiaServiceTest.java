package com.techgear.service;

import com.techgear.dto.DanhGiaSanPhamResponse;
import com.techgear.entity.DanhGia;
import com.techgear.entity.NguoiDung;
import com.techgear.entity.SanPham;
import com.techgear.repository.DanhGiaRepository;
import com.techgear.repository.SanPhamRepository;
import com.techgear.repository.DonHangRepository;
import com.techgear.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DanhGiaServiceTest {

    @Mock
    private SanPhamRepository sanPhamRepository;

    @Mock
    private DanhGiaRepository danhGiaRepository;

    @Mock
    private DonHangRepository donHangRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    private DanhGiaService danhGiaService;

    @BeforeEach
    void setUp() {
        danhGiaService = new DanhGiaService(
                sanPhamRepository, danhGiaRepository, donHangRepository, nguoiDungRepository);
    }

    @Test
    void traVeTongQuanVaDanhSachDanhGia() {
        SanPham sanPham = new SanPham();
        sanPham.setId(5L);
        sanPham.setSlug("tai-nghe");
        sanPham.setDanhMuc(new com.techgear.entity.DanhMuc());

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setHoTen("Khách hàng mẫu");

        DanhGia danhGia = new DanhGia();
        danhGia.setId(7L);
        danhGia.setNguoiDung(nguoiDung);
        danhGia.setSoSao(5);
        danhGia.setNoiDung("Sản phẩm tốt");
        danhGia.setCreatedAt(LocalDateTime.of(2026, 10, 1, 8, 0));

        PageRequest pageable = PageRequest.of(0, 5);
        when(sanPhamRepository.findBySlugAndHienThiTrue("tai-nghe")).thenReturn(Optional.of(sanPham));
        when(danhGiaRepository.findBySanPhamIdAndDaDuyetTrueOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(List.of(danhGia), pageable, 1));
        when(danhGiaRepository.tinhDiemTrungBinhDaDuyet(5L)).thenReturn(4.666);

        DanhGiaSanPhamResponse response = danhGiaService.layTheoSanPham("tai-nghe", 0, 5);

        assertEquals(4.7, response.diemTrungBinh());
        assertEquals(1, response.tongDanhGia());
        assertEquals("Khách hàng mẫu", response.danhSach().noiDung().get(0).tenNguoiDung());
        assertEquals(5, response.danhSach().noiDung().get(0).soSao());
    }

    @Test
    void sanPhamBiAnQuaDanhMucThiKhongTraDanhGia() {
        com.techgear.entity.DanhMuc danhMuc = new com.techgear.entity.DanhMuc();
        danhMuc.setHienThi(false);
        SanPham sanPham = new SanPham();
        sanPham.setId(5L);
        sanPham.setDanhMuc(danhMuc);
        when(sanPhamRepository.findBySlugAndHienThiTrue("tai-nghe")).thenReturn(Optional.of(sanPham));

        com.techgear.exception.ApiException ex = org.junit.jupiter.api.Assertions.assertThrows(
                com.techgear.exception.ApiException.class, () -> danhGiaService.layTheoSanPham("tai-nghe", 0, 5));
        assertEquals("Không tìm thấy sản phẩm", ex.getMessage());
    }
}

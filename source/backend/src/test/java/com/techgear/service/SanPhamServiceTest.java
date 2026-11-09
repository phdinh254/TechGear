package com.techgear.service;

import com.techgear.dto.SanPhamChiTietResponse;
import com.techgear.entity.DanhMuc;
import com.techgear.entity.SanPham;
import com.techgear.exception.ApiException;
import com.techgear.repository.SanPhamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SanPhamServiceTest {

    @Mock
    private SanPhamRepository sanPhamRepository;

    @Mock
    private DanhMucService danhMucService;

    private SanPhamService sanPhamService;

    @BeforeEach
    void setUp() {
        sanPhamService = new SanPhamService(sanPhamRepository, danhMucService);
    }

    @Test
    void tuChoiKhoangGiaNguoc() {
        ApiException loi = assertThrows(ApiException.class, () -> sanPhamService.timKiem(
                null, null, null,
                new BigDecimal("200000"), new BigDecimal("100000"),
                "moi_nhat", 0, 12));

        assertEquals(HttpStatus.BAD_REQUEST, loi.getStatus());
        assertEquals("Giá từ không được lớn hơn giá đến", loi.getMessage());
        verifyNoInteractions(sanPhamRepository, danhMucService);
    }

    @Test
    void chiTietSanPhamKhongBatBuocCoThuongHieu() {
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setId(10L);
        danhMuc.setTen("Phụ kiện");
        danhMuc.setSlug("phu-kien");

        SanPham sanPham = new SanPham();
        sanPham.setId(20L);
        sanPham.setDanhMuc(danhMuc);
        sanPham.setTen("Sản phẩm không thương hiệu");
        sanPham.setSlug("san-pham-khong-thuong-hieu");
        sanPham.setGiaBan(new BigDecimal("100000"));
        sanPham.setBaoHanhThang(0);
        sanPham.setLuotXem(3);

        when(sanPhamRepository.findBySlugAndHienThiTrue(sanPham.getSlug())).thenReturn(Optional.of(sanPham));

        SanPhamChiTietResponse response = sanPhamService.layChiTiet(sanPham.getSlug());

        assertNull(response.thuongHieu());
        assertEquals(4, response.luotXem());
        verify(sanPhamRepository).tangLuotXem(sanPham.getId());
    }
}

package com.techgear.service;

import com.techgear.dto.BannerResponse;
import com.techgear.entity.Banner;
import com.techgear.entity.CaiDat;
import com.techgear.repository.BannerRepository;
import com.techgear.repository.CaiDatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuaHangServiceTest {

    @Mock
    private BannerRepository bannerRepository;
    @Mock
    private CaiDatRepository caiDatRepository;

    private CuaHangService service;

    @BeforeEach
    void setUp() {
        service = new CuaHangService(bannerRepository, caiDatRepository);
    }

    @Test
    void traVeBannerDangHienThi() {
        Banner banner = new Banner();
        banner.setId(3L);
        banner.setTieuDe("Sale");
        banner.setHinhAnh("/uploads/banner/a.webp");
        banner.setThuTu(1);
        when(bannerRepository.findDangHienThi(any())).thenReturn(List.of(banner));

        List<BannerResponse> ketQua = service.layBannerDangHienThi();

        assertEquals(1, ketQua.size());
        assertEquals("/uploads/banner/a.webp", ketQua.get(0).hinhAnh());
    }

    @Test
    @SuppressWarnings("unchecked")
    void chiTruyVanCacKhoaCongKhai() {
        CaiDat ten = new CaiDat();
        ten.setKhoa("ten_cua_hang");
        ten.setGiaTri("TechGear");
        when(caiDatRepository.findByKhoaIn(any())).thenReturn(List.of(ten));

        Map<String, String> ketQua = service.layCaiDatCongKhai();

        ArgumentCaptor<Collection<String>> khoas = ArgumentCaptor.forClass(Collection.class);
        verify(caiDatRepository).findByKhoaIn(khoas.capture());
        assertEquals(CuaHangService.KHOA_CONG_KHAI, Set.copyOf(khoas.getValue()));
        assertEquals(Map.of("ten_cua_hang", "TechGear"), ketQua);
    }
}

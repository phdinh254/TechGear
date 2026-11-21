package com.techgear.service;

import com.techgear.dto.admin.AdminThuongHieuResponse;
import com.techgear.entity.ThuongHieu;
import com.techgear.exception.ApiException;
import com.techgear.repository.SanPhamRepository;
import com.techgear.repository.ThuongHieuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThuongHieuServiceTest {

    @Mock
    ThuongHieuRepository thuongHieuRepository;
    @Mock
    SanPhamRepository sanPhamRepository;
    @InjectMocks
    ThuongHieuService service;

    private static ThuongHieu thuongHieu(Long id, String ten, String slug) {
        ThuongHieu th = new ThuongHieu();
        th.setId(id);
        th.setTen(ten);
        th.setSlug(slug);
        th.setHienThi(true);
        return th;
    }

    @Test
    void xoaThuongHieuConSanPhamBiChan() {
        when(thuongHieuRepository.findById(5L)).thenReturn(Optional.of(thuongHieu(5L, "Apple", "apple")));
        when(sanPhamRepository.countByThuongHieuId(5L)).thenReturn(2L);

        ApiException ex = assertThrows(ApiException.class, () -> service.xoa(5L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("Không thể xóa: thương hiệu còn sản phẩm", ex.getMessage());
        verify(thuongHieuRepository, never()).delete(any());
    }

    @Test
    void layTatCaChoQuanTriKemSoSanPham() {
        when(thuongHieuRepository.findAll(any(Sort.class))).thenReturn(List.of(
                thuongHieu(1L, "Apple", "apple"), thuongHieu(2L, "Dell", "dell")));
        when(sanPhamRepository.demTheoThuongHieu())
                .thenReturn(Collections.singletonList(new Object[]{1L, 3L}));

        List<AdminThuongHieuResponse> kq = service.layTatCaChoQuanTri();

        assertEquals(2, kq.size());
        assertEquals(3L, kq.get(0).soSanPham());
        assertEquals(0L, kq.get(1).soSanPham());
    }
}

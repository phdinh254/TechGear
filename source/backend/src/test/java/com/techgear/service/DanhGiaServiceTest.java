package com.techgear.service;

import com.techgear.entity.SanPham;
import com.techgear.exception.ApiException;
import com.techgear.repository.DanhGiaRepository;
import com.techgear.repository.SanPhamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DanhGiaServiceTest {

    @Mock
    DanhGiaRepository danhGiaRepository;
    @Mock
    SanPhamRepository sanPhamRepository;
    @InjectMocks
    DanhGiaService service;

    @Test
    void sanPhamKhongTonTaiHoacDangAnThi404() {
        when(sanPhamRepository.findBySlugAndHienThiTrue("khong-co")).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.layTheoSanPham("khong-co", 0, 10));
    }

    @Test
    void kepTrangVaKichThuocVaSapXepMoiNhatTruoc() {
        SanPham sp = new SanPham();
        sp.setId(7L);
        when(sanPhamRepository.findBySlugAndHienThiTrue("tai-nghe")).thenReturn(Optional.of(sp));
        when(danhGiaRepository.findBySanPhamIdAndDaDuyetTrue(eq(7L), any())).thenReturn(Page.empty());

        service.layTheoSanPham("tai-nghe", -3, 1000);

        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(danhGiaRepository).findBySanPhamIdAndDaDuyetTrue(eq(7L), cap.capture());
        assertEquals(0, cap.getValue().getPageNumber());
        assertEquals(50, cap.getValue().getPageSize());
        assertEquals("createdAt: DESC,id: DESC", cap.getValue().getSort().toString());
    }
}

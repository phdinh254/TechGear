package com.techgear.service;

import com.techgear.dto.DiaChiRequest;
import com.techgear.entity.DiaChi;
import com.techgear.exception.ApiException;
import com.techgear.repository.DiaChiRepository;
import com.techgear.repository.NguoiDungRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaChiServiceTest {

    @Mock
    DiaChiRepository diaChiRepository;
    @Mock
    NguoiDungRepository nguoiDungRepository;
    @InjectMocks
    DiaChiService service;

    private static final DiaChiRequest REQ =
            new DiaChiRequest("Nguyễn Văn A", "0901234567", "Trà Vinh", "Phường 1", "12 Lê Lợi", null);

    @Test
    void diaChiDauTienTuThanhMacDinh() {
        when(diaChiRepository.countByNguoiDungId(2L)).thenReturn(0L);
        when(diaChiRepository.save(any())).thenAnswer(i -> {
            DiaChi d = i.getArgument(0);
            d.setId(1L);
            return d;
        });

        var kq = service.tao(2L, REQ);

        assertTrue(kq.laMacDinh());
        assertEquals("12 Lê Lợi, Phường 1, Trà Vinh", kq.diaChiDayDu());
    }

    @Test
    void chonMacDinhKhiThemMoiThiBoMacDinhCu() {
        DiaChi cu = diaChi(5L, true);
        when(diaChiRepository.countByNguoiDungId(2L)).thenReturn(1L);
        when(diaChiRepository.findByNguoiDungIdOrderByLaMacDinhDescIdDesc(2L)).thenReturn(List.of(cu));
        when(diaChiRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var kq = service.tao(2L, new DiaChiRequest("B", "0901234567", "Trà Vinh", "P1", "1 A", true));

        assertTrue(kq.laMacDinh());
        assertFalse(cu.getLaMacDinh());
    }

    @Test
    void khongThemQuaToiDa() {
        when(diaChiRepository.countByNguoiDungId(2L)).thenReturn((long) DiaChiService.TOI_DA);

        assertThrows(ApiException.class, () -> service.tao(2L, REQ));
        verify(diaChiRepository, never()).save(any());
    }

    @Test
    void xoaDiaChiMacDinhThiDiaChiMoiNhatConLaiThayThe() {
        DiaChi macDinh = diaChi(9L, true);
        DiaChi moiNhat = diaChi(8L, false);
        DiaChi cuHon = diaChi(3L, false);
        when(diaChiRepository.findByIdAndNguoiDungId(9L, 2L)).thenReturn(Optional.of(macDinh));
        when(diaChiRepository.findByNguoiDungIdOrderByLaMacDinhDescIdDesc(2L)).thenReturn(List.of(moiNhat, cuHon));

        service.xoa(2L, 9L);

        verify(diaChiRepository).delete(macDinh);
        assertTrue(moiNhat.getLaMacDinh());
        assertFalse(cuHon.getLaMacDinh());
    }

    @Test
    void khongTheSuaDiaChiCuaNguoiKhac() {
        when(diaChiRepository.findByIdAndNguoiDungId(7L, 2L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.capNhat(2L, 7L, REQ));
        assertEquals(404, ex.getStatus().value());
    }

    @Test
    void boChonMacDinhKhiSuaThiVanGiuMacDinh() {
        DiaChi dc = diaChi(4L, true);
        when(diaChiRepository.findByIdAndNguoiDungId(4L, 2L)).thenReturn(Optional.of(dc));

        var kq = service.capNhat(2L, 4L, new DiaChiRequest("C", "0901234567", "Trà Vinh", "P1", "2 B", false));

        assertTrue(kq.laMacDinh(), "luôn phải còn một địa chỉ mặc định");
    }

    private static DiaChi diaChi(long id, boolean macDinh) {
        DiaChi d = new DiaChi();
        d.setId(id);
        d.setLaMacDinh(macDinh);
        d.setTenNguoiNhan("X");
        d.setSoDienThoai("0900000000");
        d.setTinhThanh("T");
        d.setPhuongXa("P");
        d.setDiaChiChiTiet("D");
        return d;
    }
}

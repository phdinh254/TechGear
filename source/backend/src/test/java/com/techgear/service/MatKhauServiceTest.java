package com.techgear.service;

import com.techgear.dto.DatLaiMatKhauRequest;
import com.techgear.entity.MaDatLaiMatKhau;
import com.techgear.entity.NguoiDung;
import com.techgear.entity.enums.TrangThaiTaiKhoan;
import com.techgear.event.YeuCauDatLaiMatKhauEvent;
import com.techgear.exception.ApiException;
import com.techgear.repository.MaDatLaiMatKhauRepository;
import com.techgear.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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
class MatKhauServiceTest {

    @Mock
    NguoiDungRepository nguoiDungRepository;
    @Mock
    MaDatLaiMatKhauRepository maRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    ApplicationEventPublisher publisher;

    MatKhauService service;
    NguoiDung nd;

    @BeforeEach
    void setUp() {
        service = new MatKhauService(nguoiDungRepository, maRepository, passwordEncoder, publisher,
                "http://localhost:5173/", 15);
        nd = new NguoiDung();
        nd.setId(2L);
        nd.setEmail("khach@techgear.vn");
        nd.setHoTen("Khách");
        nd.setTrangThai(TrangThaiTaiKhoan.hoat_dong);
    }

    @Test
    void emailKhongTonTaiVanTraThongBaoChungVaKhongTaoMa() {
        when(nguoiDungRepository.findByEmail("khong-co@x.vn")).thenReturn(Optional.empty());

        assertEquals(MatKhauService.THONG_BAO_CHUNG, service.yeuCauDatLai(" Khong-Co@X.vn ").thongBao());
        verify(maRepository, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void taiKhoanBiKhoaKhongNhanMa() {
        nd.setTrangThai(TrangThaiTaiKhoan.bi_khoa);
        when(nguoiDungRepository.findByEmail(nd.getEmail())).thenReturn(Optional.of(nd));

        assertEquals(MatKhauService.THONG_BAO_CHUNG, service.yeuCauDatLai(nd.getEmail()).thongBao());
        verify(maRepository, never()).save(any());
    }

    @Test
    void taoMaMoiLuuBanBamVaGuiLinkChuaMaGoc() {
        MaDatLaiMatKhau maCu = new MaDatLaiMatKhau();
        maCu.setDaSuDung(false);
        when(nguoiDungRepository.findByEmail(nd.getEmail())).thenReturn(Optional.of(nd));
        when(maRepository.findFirstByNguoiDungIdOrderByCreatedAtDesc(2L)).thenReturn(Optional.empty());
        when(maRepository.findByNguoiDungIdAndDaSuDungFalse(2L)).thenReturn(List.of(maCu));

        service.yeuCauDatLai(nd.getEmail());

        ArgumentCaptor<MaDatLaiMatKhau> ma = ArgumentCaptor.forClass(MaDatLaiMatKhau.class);
        verify(maRepository).save(ma.capture());
        ArgumentCaptor<YeuCauDatLaiMatKhauEvent> su = ArgumentCaptor.forClass(YeuCauDatLaiMatKhauEvent.class);
        verify(publisher).publishEvent(su.capture());

        String link = su.getValue().duongDanDatLai();
        assertTrue(link.startsWith("http://localhost:5173/dat-lai-mat-khau?ma="), link);
        String maGoc = link.substring(link.indexOf("ma=") + 3);
        assertEquals(MatKhauService.bam(maGoc), ma.getValue().getMa(), "CSDL chỉ lưu bản băm của mã");
        assertTrue(ma.getValue().getHetHanLuc().isAfter(LocalDateTime.now().plusMinutes(14)));
        assertTrue(maCu.getDaSuDung(), "mã cũ chưa dùng phải bị vô hiệu");
    }

    @Test
    void yeuCauLienTiepTrong60GiayBiBoQua() {
        MaDatLaiMatKhau vuaTao = new MaDatLaiMatKhau();
        vuaTao.setCreatedAt(LocalDateTime.now().minusSeconds(10));
        when(nguoiDungRepository.findByEmail(nd.getEmail())).thenReturn(Optional.of(nd));
        when(maRepository.findFirstByNguoiDungIdOrderByCreatedAtDesc(2L)).thenReturn(Optional.of(vuaTao));

        assertEquals(MatKhauService.THONG_BAO_CHUNG, service.yeuCauDatLai(nd.getEmail()).thongBao());
        verify(maRepository, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void datLaiThanhCongDoiMatKhauVaVoHieuMa() {
        MaDatLaiMatKhau ma = maHopLe();
        when(maRepository.findByMa(MatKhauService.bam("abc"))).thenReturn(Optional.of(ma));
        when(maRepository.findByNguoiDungIdAndDaSuDungFalse(2L)).thenReturn(List.of(ma));
        when(passwordEncoder.encode("matkhaumoi")).thenReturn("$hash");

        service.datLai(new DatLaiMatKhauRequest("abc", "matkhaumoi"));

        assertEquals("$hash", nd.getPassword());
        assertTrue(ma.getDaSuDung());
    }

    @Test
    void maHetHanDaDungHoacSaiDeuBiTuChoi() {
        MaDatLaiMatKhau hetHan = maHopLe();
        hetHan.setHetHanLuc(LocalDateTime.now().minusMinutes(1));
        when(maRepository.findByMa(MatKhauService.bam("het-han"))).thenReturn(Optional.of(hetHan));
        MaDatLaiMatKhau daDung = maHopLe();
        daDung.setDaSuDung(true);
        when(maRepository.findByMa(MatKhauService.bam("da-dung"))).thenReturn(Optional.of(daDung));
        when(maRepository.findByMa(MatKhauService.bam("sai"))).thenReturn(Optional.empty());

        for (String m : List.of("het-han", "da-dung", "sai")) {
            ApiException ex = assertThrows(ApiException.class,
                    () -> service.datLai(new DatLaiMatKhauRequest(m, "matkhaumoi")));
            assertEquals(MatKhauService.LOI_MA, ex.getMessage());
        }
        assertFalse(hetHan.getDaSuDung());
    }

    private MaDatLaiMatKhau maHopLe() {
        MaDatLaiMatKhau ma = new MaDatLaiMatKhau();
        ma.setNguoiDung(nd);
        ma.setDaSuDung(false);
        ma.setHetHanLuc(LocalDateTime.now().plusMinutes(10));
        return ma;
    }
}

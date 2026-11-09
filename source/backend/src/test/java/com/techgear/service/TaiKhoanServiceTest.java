package com.techgear.service;

import com.techgear.dto.DoiMatKhauRequest;
import com.techgear.entity.NguoiDung;
import com.techgear.exception.ApiException;
import com.techgear.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaiKhoanServiceTest {

    @Mock
    NguoiDungRepository nguoiDungRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    FileStorageService fileStorageService;
    @InjectMocks
    TaiKhoanService service;

    NguoiDung nd;

    @BeforeEach
    void setUp() {
        nd = new NguoiDung();
        nd.setId(2L);
        nd.setPassword("$cu");
        when(nguoiDungRepository.findById(2L)).thenReturn(Optional.of(nd));
    }

    @Test
    void doiMatKhauSaiMatKhauCu() {
        when(passwordEncoder.matches("sai", "$cu")).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class,
                () -> service.doiMatKhau(2L, new DoiMatKhauRequest("sai", "moi123")));
        assertEquals("Mật khẩu hiện tại không đúng", ex.getMessage());
    }

    @Test
    void matKhauMoiTrungMatKhauCu() {
        when(passwordEncoder.matches("abc123", "$cu")).thenReturn(true);

        assertThrows(ApiException.class, () -> service.doiMatKhau(2L, new DoiMatKhauRequest("abc123", "abc123")));
        assertEquals("$cu", nd.getPassword());
    }

    @Test
    void doiMatKhauThanhCong() {
        when(passwordEncoder.matches("abc123", "$cu")).thenReturn(true);
        when(passwordEncoder.encode("moi123")).thenReturn("$moi");

        service.doiMatKhau(2L, new DoiMatKhauRequest("abc123", "moi123"));
        assertEquals("$moi", nd.getPassword());
    }
}

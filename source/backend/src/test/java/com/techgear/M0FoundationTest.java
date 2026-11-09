package com.techgear;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgear.dto.DangKyRequest;
import com.techgear.exception.ApiError;
import com.techgear.exception.ApiException;
import com.techgear.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M0FoundationTest {

    private static Validator validator;

    @BeforeAll
    static void khoiTaoValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void chapNhanMatKhauDuTieuChuan() {
        DangKyRequest request = taoRequest("Abcdef12");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void tuChoiMatKhauKhongDuTieuChuan() {
        assertMatKhauKhongHopLe("Abc1234");
        assertMatKhauKhongHopLe("abcdef12");
        assertMatKhauKhongHopLe("ABCDEF12");
        assertMatKhauKhongHopLe("Abcdefgh");
    }

    @Test
    void responseLoiBaoGomCacTruongThongNhat() throws Exception {
        ApiError response = ApiError.of(401, "Bạn cần đăng nhập", null);
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(response);

        assertTrue(json.contains("\"thoiGian\""));
        assertTrue(json.contains("\"trangThai\":401"));
        assertTrue(json.contains("\"thongBao\":\"Bạn cần đăng nhập\""));
        assertFalse(json.contains("\"loi\""));
    }

    @Test
    void globalHandlerGiuDungHttpStatusVaThongBao() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiError> response = handler.xuLyApiException(
                new ApiException(HttpStatus.CONFLICT, "Dữ liệu đã tồn tại"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().trangThai());
        assertEquals("Dữ liệu đã tồn tại", response.getBody().thongBao());
    }

    private static DangKyRequest taoRequest(String matKhau) {
        return new DangKyRequest("Nguyễn Văn A", "nguyenvana@example.com", matKhau, "0901234567");
    }

    private static void assertMatKhauKhongHopLe(String matKhau) {
        Set<ConstraintViolation<DangKyRequest>> viPham = validator.validate(taoRequest(matKhau));
        assertTrue(viPham.stream().anyMatch(loi -> loi.getPropertyPath().toString().equals("matKhau")));
    }
}

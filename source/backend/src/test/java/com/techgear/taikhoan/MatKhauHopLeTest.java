package com.techgear.taikhoan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatKhauHopLeTest {

    private final MatKhauHopLe.KiemTra kiemTra = new MatKhauHopLe.KiemTra();

    @Test
    void canCoChuVaSoVaDu8KyTu() {
        assertTrue(kiemTra.isValid("matkhau1", null));
        assertTrue(kiemTra.isValid("Mật khẩu 2026", null));
        assertFalse(kiemTra.isValid("mk1", null));
        assertFalse(kiemTra.isValid("chicochu", null));
        assertFalse(kiemTra.isValid("12345678", null));
        assertFalse(kiemTra.isValid(null, null));
    }

    @Test
    void toiDa72ByteVi_BCryptKhongNhanDaiHon() {
        assertTrue(kiemTra.isValid("a".repeat(71) + "1", null));
        assertFalse(kiemTra.isValid("a".repeat(72) + "1", null));
        // "ạ" chiếm 3 byte UTF-8: 24 ký tự = 72 byte, thêm 1 chữ số thành 73 byte
        assertFalse(kiemTra.isValid("ạ".repeat(24) + "1", null));
    }
}

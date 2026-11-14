package com.techgear.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SanPhamServiceTest {

    @Test
    void diemTrungBinhLamTronMotChuSoVaChuaCoDanhGiaLa0() {
        assertEquals(0, SanPhamService.lamTronMotChuSo(null));
        assertEquals(4.3, SanPhamService.lamTronMotChuSo(13 / 3.0));
        assertEquals(4.7, SanPhamService.lamTronMotChuSo(4.666));
    }
}

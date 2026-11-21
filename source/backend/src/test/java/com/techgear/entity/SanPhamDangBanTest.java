package com.techgear.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanPhamDangBanTest {

    private static DanhMuc danhMuc(boolean hienThi, DanhMuc cha) {
        DanhMuc dm = new DanhMuc();
        dm.setHienThi(hienThi);
        dm.setDanhMucCha(cha);
        return dm;
    }

    private static SanPham sanPham(DanhMuc dm) {
        SanPham sp = new SanPham();
        sp.setHienThi(true);
        sp.setDanhMuc(dm);
        return sp;
    }

    @Test
    void sanPhamVaDanhMucHienThiThiDangBan() {
        assertTrue(sanPham(danhMuc(true, null)).dangBan());
    }

    @Test
    void danhMucAnThiKhongBan() {
        assertFalse(sanPham(danhMuc(false, null)).dangBan());
    }

    @Test
    void sanPhamThuocDanhMucChaAnKhongConBan() {
        assertFalse(sanPham(danhMuc(true, danhMuc(false, null))).dangBan());
    }
}

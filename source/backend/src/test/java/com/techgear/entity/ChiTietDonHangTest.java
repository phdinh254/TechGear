package com.techgear.entity;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Sản phẩm / biến thể bị xóa mềm thì Hibernate không nạp được (do @SQLRestriction),
 * dòng đơn hàng cũ phải coi như không còn liên kết thay vì ném EntityNotFoundException (lỗi 500).
 */
class ChiTietDonHangTest {

    @Test
    void sanPhamDaXoaMemDuocBoQua() throws Exception {
        assertBoQuaKhiKhongTimThay("sanPham");
    }

    @Test
    void bienTheDaXoaMemDuocBoQua() throws Exception {
        assertBoQuaKhiKhongTimThay("bienThe");
    }

    private static void assertBoQuaKhiKhongTimThay(String truong) throws Exception {
        NotFound notFound = ChiTietDonHang.class.getDeclaredField(truong).getAnnotation(NotFound.class);
        assertNotNull(notFound, truong + " phải có @NotFound");
        assertEquals(NotFoundAction.IGNORE, notFound.action());
    }
}

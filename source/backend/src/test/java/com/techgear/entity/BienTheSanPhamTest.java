package com.techgear.entity;

import org.hibernate.annotations.DynamicUpdate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Sửa biến thể ở trang quản trị không được ghi đè so_luong_ton (tránh mất cập nhật tồn kho
 * do đơn hàng / nhập kho commit giữa lúc đọc và ghi), nên entity phải chỉ UPDATE cột thay đổi.
 */
class BienTheSanPhamTest {

    @Test
    void chiCapNhatCotThayDoi() {
        assertNotNull(BienTheSanPham.class.getAnnotation(DynamicUpdate.class),
                "BienTheSanPham phải có @DynamicUpdate");
    }
}

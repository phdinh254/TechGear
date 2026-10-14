package com.techgear.dto;

import java.math.BigDecimal;

/** Dữ liệu sản phẩm hiển thị ở trang danh sách. */
public record SanPhamTomTatResponse(
        Long id,
        String ten,
        String slug,
        String moTaNgan,
        BigDecimal giaBan,
        BigDecimal giaGoc,
        String anhChinh,
        String tenThuongHieu,
        boolean conHang
) {
}

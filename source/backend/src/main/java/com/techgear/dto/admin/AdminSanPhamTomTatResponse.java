package com.techgear.dto.admin;

import java.math.BigDecimal;

/** Một dòng trong danh sách sản phẩm của trang quản trị. */
public record AdminSanPhamTomTatResponse(
        Long id,
        String ten,
        String slug,
        String anhChinh,
        String danhMucTen,
        String thuongHieuTen,
        BigDecimal giaBan,
        BigDecimal giaGoc,
        int tongTon,
        int soBienThe,
        Boolean noiBat,
        Boolean hienThi
) {
}

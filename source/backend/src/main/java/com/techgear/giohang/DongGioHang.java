package com.techgear.giohang;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Một dòng giỏ; tên trường trùng dòng giỏ localStorage của frontend. Giá và tồn kho là giá trị hiện tại. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DongGioHang(
        Long bienTheId,
        Long sanPhamId,
        String sanPhamSlug,
        String ten,
        String phanLoai,
        String anh,
        String sku,
        long giaBan,
        Long giaGoc,
        int soLuong,
        int tonKho,
        boolean conBan
) {
}

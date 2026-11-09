package com.techgear.dto;

import java.math.BigDecimal;
import java.util.List;

public record GioHangResponse(
        List<DongGioHang> dong,
        int tongSoLuong,
        BigDecimal tamTinh
) {
    public record DongGioHang(
            Long id,
            Long bienTheId,
            String sanPhamSlug,
            String ten,
            String phanLoai,
            BigDecimal giaBan,
            String anh,
            Integer tonKho,
            Integer soLuong,
            boolean conBan
    ) {
    }
}

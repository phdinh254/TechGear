package com.techgear.dto;

import java.math.BigDecimal;

public record TinhTienResponse(
        BigDecimal tamTinh,
        BigDecimal tienGiamGia,
        BigDecimal phiVanChuyen,
        BigDecimal tongTien,
        String maGiamGia,
        String moTaGiamGia
) {
}

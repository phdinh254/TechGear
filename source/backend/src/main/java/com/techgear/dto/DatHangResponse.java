package com.techgear.dto;

import java.math.BigDecimal;

public record DatHangResponse(
        Long id,
        String maDonHang,
        BigDecimal tongTien,
        String phuongThucThanhToan,
        String trangThai,
        String thanhToanUrl
) {
}

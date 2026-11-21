package com.techgear.dto.admin;

import java.time.LocalDateTime;

public record LichSuTonKhoResponse(
        Long id,
        LocalDateTime thoiGian,
        String loai,
        int soLuongThayDoi,
        int soLuongSau,
        String ghiChu,
        String nguoiThucHien,
        String maDonHang
) {
}

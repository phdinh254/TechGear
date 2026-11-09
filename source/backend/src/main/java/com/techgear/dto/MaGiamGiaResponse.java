package com.techgear.dto;

import com.techgear.entity.MaGiamGia;
import com.techgear.entity.enums.LoaiGiam;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaGiamGiaResponse(
        String ma,
        String moTa,
        LoaiGiam loaiGiam,
        BigDecimal giaTriGiam,
        BigDecimal donToiThieu,
        BigDecimal giamToiDa,
        LocalDateTime ketThucLuc
) {
    public static MaGiamGiaResponse tu(MaGiamGia ma) {
        return new MaGiamGiaResponse(ma.getMa(), ma.getMoTa(), ma.getLoaiGiam(),
                ma.getGiaTriGiam(), ma.getDonToiThieu(), ma.getGiamToiDa(), ma.getKetThucLuc());
    }
}

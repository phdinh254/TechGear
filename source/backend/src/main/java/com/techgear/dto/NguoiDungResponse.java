package com.techgear.dto;

import com.techgear.entity.NguoiDung;

public record NguoiDungResponse(
        Long id,
        String hoTen,
        String email,
        String soDienThoai,
        String anhDaiDien,
        String vaiTro
) {
    public static NguoiDungResponse tu(NguoiDung nd) {
        return new NguoiDungResponse(nd.getId(), nd.getHoTen(), nd.getEmail(),
                nd.getSoDienThoai(), nd.getAnhDaiDien(), nd.getVaiTro().name());
    }
}

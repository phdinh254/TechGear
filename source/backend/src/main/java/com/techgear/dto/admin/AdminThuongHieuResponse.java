package com.techgear.dto.admin;

import com.techgear.entity.ThuongHieu;

public record AdminThuongHieuResponse(
        Long id,
        String ten,
        String slug,
        String logo,
        String moTa,
        Boolean hienThi,
        long soSanPham
) {
    public static AdminThuongHieuResponse tu(ThuongHieu th, long soSanPham) {
        return new AdminThuongHieuResponse(th.getId(), th.getTen(), th.getSlug(),
                th.getLogo(), th.getMoTa(), th.getHienThi(), soSanPham);
    }
}

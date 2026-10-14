package com.techgear.dto;

import com.techgear.entity.ThuongHieu;

public record ThuongHieuResponse(
        Long id,
        String ten,
        String slug,
        String logo,
        String moTa,
        Boolean hienThi
) {
    public static ThuongHieuResponse tu(ThuongHieu th) {
        if (th == null) {
            return null;
        }
        return new ThuongHieuResponse(th.getId(), th.getTen(), th.getSlug(),
                th.getLogo(), th.getMoTa(), th.getHienThi());
    }
}

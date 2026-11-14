package com.techgear.dto;

import com.techgear.entity.Banner;

public record BannerResponse(
        Long id,
        String tieuDe,
        String hinhAnh,
        String lienKet
) {
    public static BannerResponse tu(Banner b) {
        return new BannerResponse(b.getId(), b.getTieuDe(), b.getHinhAnh(), b.getLienKet());
    }
}

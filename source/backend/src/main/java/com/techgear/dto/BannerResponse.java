package com.techgear.dto;

import com.techgear.entity.Banner;

public record BannerResponse(Long id, String tieuDe, String hinhAnh, String lienKet, Integer thuTu) {

    public static BannerResponse tu(Banner banner) {
        return new BannerResponse(banner.getId(), banner.getTieuDe(), banner.getHinhAnh(),
                banner.getLienKet(), banner.getThuTu());
    }
}

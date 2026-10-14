package com.techgear.dto;

import java.util.ArrayList;
import java.util.List;

public record DanhMucResponse(
        Long id,
        String ten,
        String slug,
        String hinhAnh,
        List<DanhMucResponse> danhMucCons
) {
    public DanhMucResponse(Long id, String ten, String slug, String hinhAnh) {
        this(id, ten, slug, hinhAnh, new ArrayList<>());
    }
}

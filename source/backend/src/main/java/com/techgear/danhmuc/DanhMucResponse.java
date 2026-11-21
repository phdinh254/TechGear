package com.techgear.danhmuc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Một nút của cây danh mục; danh mục con có {@code danhMucCons} rỗng. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DanhMucResponse(
        Long id,
        String ten,
        String slug,
        String moTa,
        String hinhAnh,
        List<DanhMucResponse> danhMucCons
) {
}

package com.techgear.sanpham;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Thẻ sản phẩm ở danh sách / tìm kiếm / nổi bật. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SanPhamTomTat(
        Long id,
        String ten,
        String slug,
        long giaBan,
        Long giaGoc,
        String anhChinh,
        String tenThuongHieu,
        boolean conHang
) {
}

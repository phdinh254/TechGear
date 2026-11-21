package com.techgear.chung;

import org.springframework.data.domain.Page;

import java.util.List;

/** Kết quả phân trang trả về frontend (trang bắt đầu từ 0). */
public record PageResponse<T>(
        List<T> noiDung,
        int trang,
        int kichThuoc,
        long tongSoPhanTu,
        int tongSoTrang
) {
    public static <T> PageResponse<T> tu(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}

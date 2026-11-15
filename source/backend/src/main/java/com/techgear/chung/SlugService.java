package com.techgear.chung;

import com.techgear.chung.loi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Chọn slug không trùng trong một bảng có xóa mềm (danh_muc, thuong_hieu, san_pham). */
@Component
@RequiredArgsConstructor
public class SlugService {

    private static final Set<String> BANG = Set.of("danh_muc", "thuong_hieu", "san_pham");

    private final JdbcClient jdbc;

    /**
     * Slug nhập tay phải chưa được dùng (409); để trống thì tự sinh từ tên, trùng thì thêm -2, -3...
     * {@code boQuaId}: bản ghi đang sửa (không tính trùng với chính nó).
     */
    public String chon(String bang, String slugNhap, String ten, Long boQuaId) {
        if (!BANG.contains(bang)) {
            throw new IllegalArgumentException(bang);
        }
        if (slugNhap != null && !slugNhap.isBlank()) {
            if (daDung(bang, slugNhap, boQuaId)) {
                throw ApiException.xungDot("Slug đã được dùng");
            }
            return slugNhap;
        }
        String goc = VanBan.slug(ten);
        String slug = goc;
        for (int i = 2; daDung(bang, slug, boQuaId); i++) {
            slug = goc + "-" + i;
        }
        return slug;
    }

    private boolean daDung(String bang, String slug, Long boQuaId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM " + bang
                        + " WHERE slug = :slug AND deleted_at IS NULL AND id <> :id)")
                .param("slug", slug).param("id", boQuaId == null ? -1L : boQuaId).query(Boolean.class).single();
    }
}

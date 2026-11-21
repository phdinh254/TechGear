package com.techgear.danhmuc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DanhMucService {

    private final JdbcClient jdbc;

    private record Dong(Long id, Long danhMucChaId, String ten, String slug, String moTa, String hinhAnh) {
    }

    /** Cây 2 cấp các danh mục đang hiển thị; danh mục cha bị ẩn thì cả nhánh bị ẩn. */
    public List<DanhMucResponse> cay() {
        List<Dong> dongs = jdbc.sql("""
                        SELECT id, danh_muc_cha_id, ten, slug, mo_ta, hinh_anh
                        FROM danh_muc
                        WHERE hien_thi AND deleted_at IS NULL
                        ORDER BY thu_tu, id
                        """)
                .query(Dong.class).list();

        Map<Long, List<DanhMucResponse>> conTheoCha = new LinkedHashMap<>();
        for (Dong d : dongs) {
            if (d.danhMucChaId() != null) {
                conTheoCha.computeIfAbsent(d.danhMucChaId(), k -> new ArrayList<>()).add(nut(d, List.of()));
            }
        }
        return dongs.stream()
                .filter(d -> d.danhMucChaId() == null)
                .map(d -> nut(d, conTheoCha.getOrDefault(d.id(), List.of())))
                .toList();
    }

    private static DanhMucResponse nut(Dong d, List<DanhMucResponse> cons) {
        return new DanhMucResponse(d.id(), d.ten(), d.slug(), d.moTa(), d.hinhAnh(), cons);
    }
}

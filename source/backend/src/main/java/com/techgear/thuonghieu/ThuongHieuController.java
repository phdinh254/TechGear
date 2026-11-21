package com.techgear.thuonghieu;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
class ThuongHieuController {

    private final JdbcClient jdbc;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ThuongHieuResponse(Long id, String ten, String slug, String logo) {
    }

    @GetMapping("/api/thuong-hieu")
    List<ThuongHieuResponse> danhSach() {
        return jdbc.sql("""
                        SELECT id, ten, slug, logo FROM thuong_hieu
                        WHERE hien_thi AND deleted_at IS NULL
                        ORDER BY ten, id
                        """)
                .query(ThuongHieuResponse.class).list();
    }
}

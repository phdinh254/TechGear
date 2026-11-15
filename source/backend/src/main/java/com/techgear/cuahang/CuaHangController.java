package com.techgear.cuahang;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Banner và cấu hình công khai của cửa hàng. */
@RestController
@RequiredArgsConstructor
class CuaHangController {

    private final JdbcClient jdbc;
    private final Clock clock;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record BannerResponse(Long id, String tieuDe, String hinhAnh, String lienKet) {
    }

    /** Banner đang bật và trong khung giờ (mốc để trống = không giới hạn). */
    @GetMapping("/api/banner")
    List<BannerResponse> banner() {
        return jdbc.sql("""
                        SELECT id, tieu_de, hinh_anh, lien_ket FROM banner
                        WHERE hien_thi
                          AND (bat_dau_luc IS NULL OR bat_dau_luc <= :bayGio)
                          AND (ket_thuc_luc IS NULL OR ket_thuc_luc > :bayGio)
                        ORDER BY thu_tu, id
                        """)
                .param("bayGio", OffsetDateTime.now(clock))
                .query(BannerResponse.class).list();
    }

    @GetMapping("/api/cai-dat")
    Map<String, String> caiDat() {
        Map<String, String> ketQua = new LinkedHashMap<>();
        jdbc.sql("SELECT khoa, gia_tri FROM cai_dat ORDER BY khoa")
                .query(rs -> {
                    ketQua.put(rs.getString("khoa"), rs.getString("gia_tri"));
                });
        return ketQua;
    }
}

package com.techgear.cuahang;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Cấu hình cửa hàng (bảng cai_dat, công khai). */
@Service
@RequiredArgsConstructor
public class CaiDatService {

    public static final String PHI_VAN_CHUYEN = "phi_van_chuyen";
    public static final String MIEN_PHI_VAN_CHUYEN_TU = "mien_phi_van_chuyen_tu";

    private final JdbcClient jdbc;

    public Map<String, String> tatCa() {
        Map<String, String> ketQua = new LinkedHashMap<>();
        jdbc.sql("SELECT khoa, gia_tri FROM cai_dat ORDER BY khoa")
                .query(rs -> {
                    ketQua.put(rs.getString("khoa"), rs.getString("gia_tri"));
                });
        return ketQua;
    }

    /** Giá trị số tiền (đồng) của một khóa; khóa luôn có từ migration V3. */
    public long soTien(String khoa) {
        return Long.parseLong(jdbc.sql("SELECT gia_tri FROM cai_dat WHERE khoa = :khoa")
                .param("khoa", khoa).query(String.class).single().trim());
    }
}

package com.techgear.chung;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** Tiện ích SQL thuần cho MySQL (không có RETURNING). */
public final class Csdl {

    private Csdl() {
    }

    /** Chạy câu INSERT và trả id AUTO_INCREMENT vừa sinh. */
    public static long themLayId(JdbcClient.StatementSpec insert) {
        KeyHolder khoa = new GeneratedKeyHolder();
        insert.update(khoa);
        return khoa.getKey().longValue();
    }
}

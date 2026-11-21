package com.techgear;

import com.techgear.util.SkuUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkuUtilTest {

    @Test
    void giaiPhongThemHauToId() {
        assertEquals("ABC#7", SkuUtil.giaiPhong("ABC", 7));
    }

    @Test
    void giaiPhongCatBotKhiQuaDai() {
        String sku = "A".repeat(100);
        String kq = SkuUtil.giaiPhong(sku, 123);
        assertEquals(100, kq.length());
        assertTrue(kq.endsWith("#123"));
    }
}

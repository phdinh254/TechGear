package com.techgear;

import com.techgear.util.SlugUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlugUtilTest {

    @Test
    void taoSlugTuTiengVietCoDau() {
        assertEquals("cu-sac-nhanh-gan-65w", SlugUtil.taoSlug("Củ sạc nhanh GaN 65W"));
        assertEquals("op-lung-bao-ve", SlugUtil.taoSlug("Ốp lưng & Bảo vệ"));
        assertEquals("dien-thoai", SlugUtil.taoSlug("  Điện thoại  "));
    }
}

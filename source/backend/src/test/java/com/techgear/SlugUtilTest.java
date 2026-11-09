package com.techgear;

import com.techgear.util.SlugUtil;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlugUtilTest {

    @Test
    void taoSlugTuTiengVietCoDau() {
        assertEquals("cu-sac-nhanh-gan-65w", SlugUtil.taoSlug("Củ sạc nhanh GaN 65W"));
        assertEquals("op-lung-bao-ve", SlugUtil.taoSlug("Ốp lưng & Bảo vệ"));
        assertEquals("dien-thoai", SlugUtil.taoSlug("  Điện thoại  "));
    }

    @Test
    void taoSlugDuyNhatThemHauToKhiTrung() {
        Set<String> daCo = Set.of("loa", "loa-2");
        assertEquals("loa-3", SlugUtil.taoSlugDuyNhat("Loa", daCo::contains));
        assertEquals("tai-nghe", SlugUtil.taoSlugDuyNhat("Tai nghe", daCo::contains));
        assertEquals("muc", SlugUtil.taoSlugDuyNhat("!!!", daCo::contains));
    }

    @Test
    void cungGocNhanRaSlugCoHauToSo() {
        assertTrue(SlugUtil.cungGoc("loa", "loa"));
        assertTrue(SlugUtil.cungGoc("loa-12", "loa"));
        assertFalse(SlugUtil.cungGoc("loa-bluetooth", "loa"));
        assertFalse(SlugUtil.cungGoc(null, "loa"));
    }
}

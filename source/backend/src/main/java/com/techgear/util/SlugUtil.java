package com.techgear.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Tạo slug từ tiếng Việt có dấu: "Củ sạc nhanh 65W" -> "cu-sac-nhanh-65w". */
public final class SlugUtil {

    private static final Pattern DAU = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern KY_TU_KHAC = Pattern.compile("[^a-z0-9]+");

    private SlugUtil() {
    }

    public static String taoSlug(String chuoi) {
        if (chuoi == null) {
            return "";
        }
        String s = chuoi.trim().replace('đ', 'd').replace('Đ', 'D');
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = DAU.matcher(s).replaceAll("");
        s = s.toLowerCase(Locale.ROOT);
        s = KY_TU_KHAC.matcher(s).replaceAll("-");
        return s.replaceAll("^-+|-+$", "");
    }
}

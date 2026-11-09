package com.techgear.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;
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

    /**
     * Tạo slug chưa bị dùng: "sac-du-phong", nếu trùng thì "sac-du-phong-2", "sac-du-phong-3"...
     *
     * @param daDung trả về true nếu slug đã có trong CSDL (kể cả bản ghi đã xóa mềm, vì cột slug UNIQUE)
     */
    public static String taoSlugDuyNhat(String chuoi, Predicate<String> daDung) {
        String goc = taoSlug(chuoi);
        if (goc.isEmpty()) {
            goc = "muc";
        }
        String slug = goc;
        for (int i = 2; daDung.test(slug); i++) {
            slug = goc + "-" + i;
        }
        return slug;
    }

    /** true nếu slugHienTai là slugGoc hoặc slugGoc kèm hậu tố số ("loa", "loa-2"): khi sửa tên thì giữ slug cũ. */
    public static boolean cungGoc(String slugHienTai, String slugGoc) {
        return slugHienTai != null && slugHienTai.matches(Pattern.quote(slugGoc) + "(-\\d+)?");
    }
}

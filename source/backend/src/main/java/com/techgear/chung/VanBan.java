package com.techgear.chung;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.text.Normalizer;
import java.util.Locale;

/** Xử lý văn bản tiếng Việt: bỏ dấu, tạo slug, lọc HTML do quản trị nhập. */
public final class VanBan {

    private static final int DO_DAI_SLUG = 100;
    /** Thẻ an toàn cho mô tả; giữ đường dẫn tương đối để ảnh /uploads/... chèn trong mô tả vẫn hiện. */
    private static final Safelist THE_AN_TOAN = Safelist.relaxed().preserveRelativeLinks(true);

    private VanBan() {
    }

    public static String boDau(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D');
    }

    /** "Củ sạc Anker 65W" → "cu-sac-anker-65w"; tối đa 100 ký tự; không còn chữ số nào thì "san-pham". */
    public static String slug(String ten) {
        String s = boDau(ten).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (s.length() > DO_DAI_SLUG) {
            s = s.substring(0, DO_DAI_SLUG).replaceAll("-+$", "");
        }
        return s.isEmpty() ? "san-pham" : s;
    }

    /** Bỏ script, thuộc tính on*, liên kết javascript:...; rỗng sau khi lọc thì trả null. */
    public static String locHtml(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        String sach = Jsoup.clean(html, "http://localhost/", THE_AN_TOAN).trim();
        return sach.isEmpty() ? null : sach;
    }
}

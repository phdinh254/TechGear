package com.techgear.chung;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VanBanTest {

    @Test
    void slugBoDauVaKyTuLa() {
        assertThat(VanBan.slug("Củ sạc Anker Nano 65W GaN")).isEqualTo("cu-sac-anker-nano-65w-gan");
        assertThat(VanBan.slug("Đế sạc không dây – ĐEN")).isEqualTo("de-sac-khong-day-den");
        assertThat(VanBan.slug("  Ốp lưng & bảo vệ!! ")).isEqualTo("op-lung-bao-ve");
        assertThat(VanBan.slug("Tai nghe Bluetooth 5.3")).isEqualTo("tai-nghe-bluetooth-5-3");
        assertThat(VanBan.slug("!!!")).isEqualTo("san-pham");
    }

    @Test
    void slugKhongQuaDai() {
        assertThat(VanBan.slug("a".repeat(300))).hasSize(100);
        assertThat(VanBan.slug("ab ".repeat(60))).doesNotEndWith("-").hasSizeLessThanOrEqualTo(100);
    }

    @Test
    void locHtmlBoScriptVaThuocTinhNguyHiem() {
        String sach = VanBan.locHtml("""
                <h2>Đặc điểm</h2><p onclick="alert(1)">Pin <b>30 giờ</b> <a href="javascript:alert(1)">xem</a>
                <a href="https://vi-du.vn">link</a></p><script>alert(1)</script>
                <img src="https://vi-du.vn/a.png" onerror="alert(1)"><iframe src="https://x"></iframe>
                """);
        assertThat(sach).contains("<h2>Đặc điểm</h2>", "<b>30 giờ</b>", "href=\"https://vi-du.vn\"",
                "<img src=\"https://vi-du.vn/a.png\">");
        assertThat(sach).doesNotContain("script", "onclick", "onerror", "javascript:", "iframe");
    }

    @Test
    void locHtmlRongThanhNull() {
        assertThat(VanBan.locHtml(null)).isNull();
        assertThat(VanBan.locHtml("   ")).isNull();
        assertThat(VanBan.locHtml("<script>x</script>")).isNull();
    }
}

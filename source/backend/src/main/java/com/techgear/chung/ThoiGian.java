package com.techgear.chung;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/** Múi giờ nghiệp vụ của cửa hàng (Việt Nam). */
public final class ThoiGian {

    public static final ZoneId VIET_NAM = ZoneId.of("Asia/Ho_Chi_Minh");

    private ThoiGian() {
    }

    public static OffsetDateTime bayGio() {
        return OffsetDateTime.now(VIET_NAM);
    }
}

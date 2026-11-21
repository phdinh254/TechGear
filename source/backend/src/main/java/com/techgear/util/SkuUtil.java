package com.techgear.util;

/** Tiện ích cho SKU biến thể. */
public final class SkuUtil {

    private static final int DO_DAI_TOI_DA = 100;

    private SkuUtil() {
    }

    /**
     * Giải phóng SKU của biến thể bị xóa mềm để SKU đó dùng lại được: "ABC" + id 7 -> "ABC#7".
     * SKU hợp lệ không chứa '#', nên kết quả không bao giờ trùng một SKU mới. Tối đa 100 ký tự.
     */
    public static String giaiPhong(String sku, long id) {
        String hauTo = "#" + id;
        int toiDa = DO_DAI_TOI_DA - hauTo.length();
        String goc = sku.length() > toiDa ? sku.substring(0, toiDa) : sku;
        return goc + hauTo;
    }
}

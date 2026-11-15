package com.techgear.sanpham;

/** Cách sắp xếp kết quả tìm kiếm; cùng giá trị thì xếp theo id giảm dần để phân trang ổn định. */
public enum SapXep {
    moi_nhat("sp.created_at DESC, sp.id DESC"),
    gia_tang("sp.gia_ban ASC, sp.id DESC"),
    gia_giam("sp.gia_ban DESC, sp.id DESC"),
    pho_bien("sp.luot_xem DESC, sp.id DESC");

    final String orderBy;

    SapXep(String orderBy) {
        this.orderBy = orderBy;
    }
}

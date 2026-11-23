package com.techgear.thanhtoan;

/** Lời gọi API PayOS (https://payos.vn/docs/api/); tách interface để giả lập ở dev và thay bằng bản giả khi kiểm thử. */
public interface PayOsClient {

    /** Dữ liệu tạo link; {@code expiredAt} là Unix giây. */
    record YeuCauLink(long orderCode, long amount, String description, String returnUrl, String cancelUrl, long expiredAt) {
    }

    record LinkThanhToan(String checkoutUrl, String paymentLinkId) {
    }

    /** {@code status}: PENDING | PROCESSING | PAID | CANCELLED | EXPIRED. */
    record ThongTinLink(long orderCode, long amount, long amountPaid, String status) {
    }

    LinkThanhToan taoLink(YeuCauLink yc);

    ThongTinLink layThongTin(long orderCode);
}

package com.techgear.thanhtoan;

import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * PayOS giả lập cho máy cá nhân (xem {@link PayOsProperties#dungGiaLap()}):
 * link thanh toán dẫn thẳng về trang kết quả, hỏi lại luôn báo PAID đủ tiền.
 */
class PayOsClientGiaLap implements PayOsClient {

    private final JdbcClient jdbc;

    PayOsClientGiaLap(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public LinkThanhToan taoLink(YeuCauLink yc) {
        return new LinkThanhToan(yc.returnUrl() + "?orderCode=" + yc.orderCode() + "&status=PAID&gia_lap=1", "gia-lap-" + yc.orderCode());
    }

    @Override
    public ThongTinLink layThongTin(long orderCode) {
        long soTien = jdbc.sql("SELECT so_tien FROM thanh_toan WHERE ma_tham_chieu = :ma")
                .param("ma", String.valueOf(orderCode)).query(Long.class).optional().orElse(0L);
        return new ThongTinLink(orderCode, soTien, soTien, "PAID");
    }
}

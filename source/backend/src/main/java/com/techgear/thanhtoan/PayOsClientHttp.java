package com.techgear.thanhtoan;

import com.techgear.chung.loi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/** Gọi PayOS thật qua HTTP (header x-client-id, x-api-key). Mọi lỗi từ PayOS thành 502 cho phía gọi. */
@Slf4j
class PayOsClientHttp implements PayOsClient {

    private final PayOsProperties payOs;
    private final RestClient http;

    /** Thời gian chờ mặc định: lời gọi PayOS chạy khi đang giữ khóa dòng CSDL nên không được treo. */
    static final Duration CHO_MAC_DINH = Duration.ofSeconds(10);

    PayOsClientHttp(PayOsProperties payOs) {
        this(payOs, CHO_MAC_DINH);
    }

    PayOsClientHttp(PayOsProperties payOs, Duration thoiGianCho) {
        this.payOs = payOs;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(thoiGianCho).build());
        factory.setReadTimeout(thoiGianCho);
        this.http = RestClient.builder().requestFactory(factory).baseUrl(payOs.apiUrl())
                .defaultHeader("x-client-id", payOs.clientId() == null ? "" : payOs.clientId())
                .defaultHeader("x-api-key", payOs.apiKey() == null ? "" : payOs.apiKey())
                .build();
    }

    record PhanHoi(String code, String desc, Map<String, Object> data) {
        boolean thanhCong() {
            return "00".equals(code) && data != null;
        }
    }

    @Override
    public LinkThanhToan taoLink(YeuCauLink yc) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderCode", yc.orderCode());
        body.put("amount", yc.amount());
        body.put("description", yc.description());
        body.put("cancelUrl", yc.cancelUrl());
        body.put("returnUrl", yc.returnUrl());
        body.put("expiredAt", yc.expiredAt());
        body.put("signature", ChuKyPayOs.kyTaoLink(yc.amount(), yc.cancelUrl(), yc.description(), yc.orderCode(),
                yc.returnUrl(), payOs.checksumKey()));
        PhanHoi ph = goi(() -> http.post().uri("/v2/payment-requests").contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(PhanHoi.class), "Không tạo được liên kết thanh toán PayOS", yc.orderCode());
        if (!(ph.data().get("checkoutUrl") instanceof String checkoutUrl) || checkoutUrl.isBlank()) {
            log.error("PayOS không trả checkoutUrl, orderCode {}", yc.orderCode());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Không tạo được liên kết thanh toán PayOS");
        }
        return new LinkThanhToan(checkoutUrl, String.valueOf(ph.data().get("paymentLinkId")));
    }

    @Override
    public ThongTinLink layThongTin(long orderCode) {
        PhanHoi ph = goi(() -> http.get().uri("/v2/payment-requests/{id}", orderCode).retrieve().body(PhanHoi.class),
                "Không kiểm tra được giao dịch PayOS", orderCode);
        Map<String, Object> d = ph.data();
        return new ThongTinLink(orderCode, so(d.get("amount")), so(d.get("amountPaid")), String.valueOf(d.get("status")));
    }

    private PhanHoi goi(java.util.function.Supplier<PhanHoi> loiGoi, String thongBao, long orderCode) {
        PhanHoi ph;
        try {
            ph = loiGoi.get();
        } catch (RestClientException e) {
            log.error("Lỗi gọi PayOS, orderCode {}", orderCode, e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, thongBao);
        }
        if (ph == null || !ph.thanhCong()) {
            log.error("PayOS từ chối, orderCode {}: {} {}", orderCode, ph == null ? null : ph.code(), ph == null ? null : ph.desc());
            throw new ApiException(HttpStatus.BAD_GATEWAY, thongBao);
        }
        return ph;
    }

    private static long so(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }
}

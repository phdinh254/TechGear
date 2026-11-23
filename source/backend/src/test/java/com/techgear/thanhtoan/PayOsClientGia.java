package com.techgear.thanhtoan;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** PayOS giả cho kiểm thử tích hợp: ghi lại yêu cầu tạo link, trạng thái trả về khi hỏi lại do test đặt. */
@TestConfiguration
public class PayOsClientGia {

    public static final class Gia implements PayOsClient {
        final List<YeuCauLink> yeuCau = new CopyOnWriteArrayList<>();
        final Map<Long, Long> soTien = new ConcurrentHashMap<>();
        final AtomicInteger soLanHoi = new AtomicInteger();
        volatile String trangThai = "PENDING";
        volatile boolean loi;

        void datLai() {
            yeuCau.clear();
            soTien.clear();
            soLanHoi.set(0);
            trangThai = "PENDING";
            loi = false;
        }

        @Override
        public LinkThanhToan taoLink(YeuCauLink yc) {
            yeuCau.add(yc);
            soTien.put(yc.orderCode(), yc.amount());
            return new LinkThanhToan("https://pay.payos.vn/web/gia-" + yc.orderCode(), "gia-" + yc.orderCode());
        }

        @Override
        public ThongTinLink layThongTin(long orderCode) {
            soLanHoi.incrementAndGet();
            if (loi) {
                throw new com.techgear.chung.loi.ApiException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Không kiểm tra được giao dịch PayOS");
            }
            long tien = soTien.getOrDefault(orderCode, 0L);
            return new ThongTinLink(orderCode, tien, "PAID".equals(trangThai) ? tien : 0, trangThai);
        }
    }

    @Bean
    @Primary
    Gia payOsClientGia() {
        return new Gia();
    }
}

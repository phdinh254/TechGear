package com.techgear.thanhtoan;

import com.sun.net.httpserver.HttpServer;
import com.techgear.chung.loi.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayOsClientHttpTest {

    HttpServer server;

    PayOsProperties cauHinh() {
        return new PayOsProperties("id", "key", "checksum", "http://localhost:" + server.getAddress().getPort(),
                "http://fe/ket-qua", "http://fe/ket-qua", false);
    }

    void phucVu(int treMs, String json) throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", ex -> {
            try {
                Thread.sleep(treMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] b = json.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        server.start();
    }

    @AfterEach
    void dung() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void payOsTreQuaThoiGianCho_bao502NgayKhongTreo() throws Exception {
        phucVu(3000, "{\"code\":\"00\",\"data\":{\"status\":\"PAID\"}}");
        PayOsClientHttp client = new PayOsClientHttp(cauHinh(), Duration.ofMillis(500));

        long batDau = System.nanoTime();
        assertThatThrownBy(() -> client.layThongTin(4301))
                .isInstanceOf(ApiException.class).hasMessage("Không kiểm tra được giao dịch PayOS");
        assertThat(Duration.ofNanos(System.nanoTime() - batDau)).isLessThan(Duration.ofSeconds(2));
    }

    @Test
    void taoLink_thieuCheckoutUrl_bao502() throws Exception {
        phucVu(0, "{\"code\":\"00\",\"data\":{\"paymentLinkId\":\"x\"}}");
        PayOsClientHttp client = new PayOsClientHttp(cauHinh(), Duration.ofSeconds(2));

        assertThatThrownBy(() -> client.taoLink(new PayOsClient.YeuCauLink(4301, 2000, "TG4301", "http://fe/r", "http://fe/c", 1)))
                .isInstanceOf(ApiException.class).hasMessage("Không tạo được liên kết thanh toán PayOS");
    }
}

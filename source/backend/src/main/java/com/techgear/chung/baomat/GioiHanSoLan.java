package com.techgear.chung.baomat;

import com.techgear.chung.loi.ApiException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giới hạn số lần thử theo khóa (VD "dang-nhap:email|ip") trong một cửa sổ thời gian trượt.
 * shortcut: lưu trong bộ nhớ một tiến trình – chạy nhiều máy chủ thì chuyển sang kho dùng chung (Redis/CSDL).
 */
@Component
public class GioiHanSoLan {

    private static final int TOI_DA_MOI_KHOA = 100;
    private static final int TOI_DA_SO_KHOA = 10_000;
    private static final Duration GIU_TOI_DA = Duration.ofHours(1);

    private final Clock clock;
    private final Map<String, Deque<Instant>> lanThu = new ConcurrentHashMap<>();

    public GioiHanSoLan(Clock clock) {
        this.clock = clock;
    }

    /**
     * Kiểm tra và ghi nhận một lần thử trong cùng một khóa đồng bộ (gửi song song không vượt được giới hạn).
     * Ném 429 nếu khóa đã có từ {@code toiDa} lần trong {@code cuaSo}; trả mốc để {@link #traLai} khi lần đó không tính.
     */
    public Instant giuCho(String khoa, int toiDa, Duration cuaSo) {
        if (lanThu.size() > TOI_DA_SO_KHOA) {
            donKhoaCu();
        }
        Deque<Instant> ds = lanThu.computeIfAbsent(khoa, k -> new ArrayDeque<>());
        Instant bayGio = clock.instant();
        Instant moc = bayGio.minus(cuaSo);
        synchronized (ds) {
            if (ds.stream().filter(t -> t.isAfter(moc)).count() >= toiDa) {
                throw ApiException.quaNhieuYeuCau("Bạn thao tác quá nhiều lần, vui lòng thử lại sau ít phút");
            }
            ds.addLast(bayGio);
            while (ds.size() > TOI_DA_MOI_KHOA) {
                ds.removeFirst();
            }
        }
        return bayGio;
    }

    /** Bỏ lần đã giữ chỗ (VD đăng nhập đúng, tra cứu thấy đơn) để nó không bị tính. */
    public void traLai(String khoa, Instant lan) {
        Deque<Instant> ds = lanThu.get(khoa);
        if (ds != null) {
            synchronized (ds) {
                ds.removeLastOccurrence(lan);
            }
        }
    }

    public void xoa(String khoa) {
        lanThu.remove(khoa);
    }

    private void donKhoaCu() {
        Instant moc = clock.instant().minus(GIU_TOI_DA);
        lanThu.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast().isBefore(moc);
            }
        });
    }
}

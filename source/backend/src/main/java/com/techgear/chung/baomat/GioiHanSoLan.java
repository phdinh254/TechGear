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

    /** Ném 429 nếu khóa đã có từ {@code toiDa} lần thử trở lên trong {@code cuaSo} gần nhất. */
    public void kiemTra(String khoa, int toiDa, Duration cuaSo) {
        Deque<Instant> ds = lanThu.get(khoa);
        if (ds == null) {
            return;
        }
        Instant moc = clock.instant().minus(cuaSo);
        synchronized (ds) {
            long trongCuaSo = ds.stream().filter(t -> t.isAfter(moc)).count();
            if (trongCuaSo >= toiDa) {
                throw ApiException.quaNhieuYeuCau("Bạn thao tác quá nhiều lần, vui lòng thử lại sau ít phút");
            }
        }
    }

    public void ghiNhan(String khoa) {
        if (lanThu.size() > TOI_DA_SO_KHOA) {
            donKhoaCu();
        }
        Deque<Instant> ds = lanThu.computeIfAbsent(khoa, k -> new ArrayDeque<>());
        synchronized (ds) {
            ds.addLast(clock.instant());
            while (ds.size() > TOI_DA_MOI_KHOA) {
                ds.removeFirst();
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

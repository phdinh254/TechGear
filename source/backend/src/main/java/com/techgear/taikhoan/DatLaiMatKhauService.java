package com.techgear.taikhoan;

import com.techgear.chung.baomat.GioiHanSoLan;
import com.techgear.chung.loi.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

/** Quên mật khẩu (gửi liên kết có mã một lần) và đặt lại mật khẩu bằng mã đó. */
@Service
public class DatLaiMatKhauService {

    static final Duration HIEU_LUC_MA = Duration.ofMinutes(15);
    static final String MA_KHONG_HOP_LE = "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn";

    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    private final NguoiDungRepository nguoiDungRepository;
    private final MaDatLaiMatKhauRepository maRepository;
    private final TaiKhoanService taiKhoanService;
    private final GioiHanSoLan gioiHanSoLan;
    private final ApplicationEventPublisher suKien;
    private final Clock clock;
    private final String frontendUrl;

    public DatLaiMatKhauService(NguoiDungRepository nguoiDungRepository, MaDatLaiMatKhauRepository maRepository,
                                TaiKhoanService taiKhoanService, GioiHanSoLan gioiHanSoLan,
                                ApplicationEventPublisher suKien, Clock clock,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.maRepository = maRepository;
        this.taiKhoanService = taiKhoanService;
        this.gioiHanSoLan = gioiHanSoLan;
        this.suKien = suKien;
        this.clock = clock;
        this.frontendUrl = frontendUrl;
    }

    /** Kết quả như nhau dù email có tồn tại hay không (chỉ khác khi vượt giới hạn số lần – áp cho mọi email). */
    @Transactional
    public void quenMatKhau(String emailVao, String ip) {
        String email = TaiKhoanService.chuanHoaEmail(emailVao);
        String khoaIp = "quen-mat-khau-ip:" + ip;
        String khoaEmail = "quen-mat-khau:" + email;
        gioiHanSoLan.kiemTra(khoaIp, 10, Duration.ofMinutes(15));
        gioiHanSoLan.kiemTra(khoaEmail, 1, Duration.ofSeconds(60));
        gioiHanSoLan.ghiNhan(khoaIp);
        gioiHanSoLan.ghiNhan(khoaEmail);

        nguoiDungRepository.findByEmail(email)
                .filter(n -> n.getTrangThai() == NguoiDung.TrangThai.hoat_dong)
                .ifPresent(n -> {
                    byte[] ngauNhien = new byte[32];
                    NGAU_NHIEN.nextBytes(ngauNhien);
                    String ma = Base64.getUrlEncoder().withoutPadding().encodeToString(ngauNhien);

                    MaDatLaiMatKhau m = new MaDatLaiMatKhau();
                    m.setNguoiDungId(n.getId());
                    m.setMaBam(bam(ma));
                    m.setHetHanLuc(OffsetDateTime.now(clock).plus(HIEU_LUC_MA));
                    maRepository.save(m);
                    suKien.publishEvent(new YeuCauDatLaiMatKhau(n.getEmail(), n.getHoTen(),
                            frontendUrl + "/dat-lai-mat-khau?ma=" + ma));
                });
    }

    /** Đặt mật khẩu mới; mọi mã khác của tài khoản và mọi token cũ hết hiệu lực. */
    @Transactional
    public void datLaiMatKhau(DatLaiMatKhauRequest req) {
        MaDatLaiMatKhau m = maRepository.findByMaBam(bam(req.ma()))
                .orElseThrow(() -> ApiException.yeuCauKhongHopLe(MA_KHONG_HOP_LE));
        if (maRepository.suDung(m.getId(), OffsetDateTime.now(clock)) == 0) {
            throw ApiException.yeuCauKhongHopLe(MA_KHONG_HOP_LE);
        }
        NguoiDung n = nguoiDungRepository.findById(m.getNguoiDungId())
                .orElseThrow(() -> ApiException.yeuCauKhongHopLe(MA_KHONG_HOP_LE));
        taiKhoanService.datMatKhau(n, req.matKhauMoi());
        maRepository.huyTatCa(n.getId());
    }

    static String bam(String ma) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(ma.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // JVM nào cũng có SHA-256
        }
    }
}

package com.techgear.service;

import com.techgear.dto.DatLaiMatKhauRequest;
import com.techgear.dto.ThongBaoResponse;
import com.techgear.entity.MaDatLaiMatKhau;
import com.techgear.entity.NguoiDung;
import com.techgear.entity.enums.TrangThaiTaiKhoan;
import com.techgear.event.YeuCauDatLaiMatKhauEvent;
import com.techgear.exception.ApiException;
import com.techgear.repository.MaDatLaiMatKhauRepository;
import com.techgear.repository.NguoiDungRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Quên mật khẩu / đặt lại mật khẩu.
 * - Mã gửi qua email là chuỗi ngẫu nhiên 256 bit; CSDL chỉ lưu bản băm SHA-256 của mã.
 * - Mã hết hạn sau app.reset-password.expiration-minutes phút, chỉ dùng một lần.
 * - Luôn trả cùng một thông báo dù email có tồn tại hay không (không để lộ email nào đã đăng ký).
 */
@Slf4j
@Service
public class MatKhauService {

    static final String THONG_BAO_CHUNG =
            "Nếu email đã đăng ký tài khoản, chúng tôi đã gửi liên kết đặt lại mật khẩu. Vui lòng kiểm tra hộp thư.";
    static final String LOI_MA = "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn";
    private static final int GIAY_GIUA_HAI_LAN_YEU_CAU = 60;
    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    private final NguoiDungRepository nguoiDungRepository;
    private final MaDatLaiMatKhauRepository maRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;
    private final String frontendUrl;
    private final long hieuLucPhut;

    public MatKhauService(NguoiDungRepository nguoiDungRepository, MaDatLaiMatKhauRepository maRepository,
                          PasswordEncoder passwordEncoder, ApplicationEventPublisher publisher,
                          @Value("${app.frontend-url}") String frontendUrl,
                          @Value("${app.reset-password.expiration-minutes:15}") long hieuLucPhut) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.maRepository = maRepository;
        this.passwordEncoder = passwordEncoder;
        this.publisher = publisher;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.hieuLucPhut = hieuLucPhut;
    }

    @Transactional
    public ThongBaoResponse yeuCauDatLai(String email) {
        String e = email.trim().toLowerCase(Locale.ROOT);
        nguoiDungRepository.findByEmail(e)
                .filter(nd -> nd.getTrangThai() == TrangThaiTaiKhoan.hoat_dong)
                .ifPresent(this::taoVaGuiMa);
        return new ThongBaoResponse(THONG_BAO_CHUNG);
    }

    @Transactional
    public ThongBaoResponse datLai(DatLaiMatKhauRequest req) {
        LocalDateTime bayGio = LocalDateTime.now();
        MaDatLaiMatKhau ma = maRepository.findByMa(bam(req.ma().trim()))
                .filter(m -> !Boolean.TRUE.equals(m.getDaSuDung()) && m.getHetHanLuc().isAfter(bayGio))
                .orElseThrow(() -> ApiException.yeuCauKhongHopLe(LOI_MA));
        NguoiDung nd = ma.getNguoiDung();
        if (nd.getTrangThai() == TrangThaiTaiKhoan.bi_khoa) {
            throw ApiException.khongCoQuyen("Tài khoản đã bị khóa");
        }
        nd.setPassword(passwordEncoder.encode(req.matKhauMoi()));
        voHieuCacMaChuaDung(nd.getId());
        return new ThongBaoResponse("Đặt lại mật khẩu thành công, vui lòng đăng nhập bằng mật khẩu mới");
    }

    private void taoVaGuiMa(NguoiDung nd) {
        LocalDateTime bayGio = LocalDateTime.now();
        boolean vuaYeuCau = maRepository.findFirstByNguoiDungIdOrderByCreatedAtDesc(nd.getId())
                .map(m -> m.getCreatedAt().isAfter(bayGio.minusSeconds(GIAY_GIUA_HAI_LAN_YEU_CAU)))
                .orElse(false);
        if (vuaYeuCau) {
            log.info("Bỏ qua yêu cầu đặt lại mật khẩu của người dùng {}: chưa đủ {} giây kể từ lần trước",
                    nd.getId(), GIAY_GIUA_HAI_LAN_YEU_CAU);
            return;
        }
        voHieuCacMaChuaDung(nd.getId());

        byte[] byteNgauNhien = new byte[32];
        NGAU_NHIEN.nextBytes(byteNgauNhien);
        String maGoc = Base64.getUrlEncoder().withoutPadding().encodeToString(byteNgauNhien);

        MaDatLaiMatKhau ma = new MaDatLaiMatKhau();
        ma.setNguoiDung(nd);
        ma.setMa(bam(maGoc));
        ma.setHetHanLuc(bayGio.plusMinutes(hieuLucPhut));
        ma.setDaSuDung(false);
        maRepository.save(ma);

        publisher.publishEvent(new YeuCauDatLaiMatKhauEvent(nd.getEmail(), nd.getHoTen(),
                frontendUrl + "/dat-lai-mat-khau?ma=" + maGoc, hieuLucPhut));
    }

    private void voHieuCacMaChuaDung(Long nguoiDungId) {
        maRepository.findByNguoiDungIdAndDaSuDungFalse(nguoiDungId).forEach(m -> m.setDaSuDung(true));
    }

    /** SHA-256 dạng hex (64 ký tự) – vừa cột ma VARCHAR(100). */
    static String bam(String maGoc) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(maGoc.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

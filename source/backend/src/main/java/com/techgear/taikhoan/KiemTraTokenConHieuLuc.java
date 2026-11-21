package com.techgear.taikhoan;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Từ chối token của tài khoản không còn, bị khóa, hoặc phát hành trước lần đổi/đặt lại mật khẩu gần nhất.
 * iat của JWT tính theo giây nên mốc đổi mật khẩu cũng cắt về giây (token mới phát hành cùng giây vẫn hợp lệ).
 * BaoMatConfig gắn mọi bean OAuth2TokenValidator&lt;Jwt&gt; vào bộ giải mã JWT.
 */
@Component
@RequiredArgsConstructor
class KiemTraTokenConHieuLuc implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2TokenValidatorResult THU_HOI = OAuth2TokenValidatorResult.failure(
            new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Token đã bị thu hồi", null));

    private final NguoiDungRepository nguoiDungRepository;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Instant phatHanh = jwt.getIssuedAt();
        boolean conHieuLuc = phatHanh != null && nguoiDungRepository.findById(Long.valueOf(jwt.getSubject()))
                .filter(n -> n.getTrangThai() == NguoiDung.TrangThai.hoat_dong)
                .filter(n -> n.getDoiMatKhauLuc() == null
                        || !phatHanh.isBefore(n.getDoiMatKhauLuc().toInstant().truncatedTo(ChronoUnit.SECONDS)))
                .isPresent();
        return conHieuLuc ? OAuth2TokenValidatorResult.success() : THU_HOI;
    }
}

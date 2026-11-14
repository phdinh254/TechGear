package com.techgear.chung.baomat;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Phát hành JWT (HS256): sub = mã người dùng, claim vaiTro = quan_tri | khach_hang. */
@Service
public class JwtService {

    public static final String NHA_PHAT_HANH = "techgear";
    public static final String CLAIM_VAI_TRO = "vaiTro";

    private final JwtEncoder jwtEncoder;
    private final Duration thoiHan;
    private final Clock clock;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.thoiHan = properties.thoiHan();
        this.clock = clock;
    }

    public String taoToken(Long nguoiDungId, String vaiTro) {
        Instant bayGio = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(NHA_PHAT_HANH)
                .subject(nguoiDungId.toString())
                .issuedAt(bayGio)
                .expiresAt(bayGio.plus(thoiHan))
                .claim(CLAIM_VAI_TRO, vaiTro)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}

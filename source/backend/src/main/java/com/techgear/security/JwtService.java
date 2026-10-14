package com.techgear.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/** Tạo và kiểm tra JWT (thuật toán HMAC-SHA). */
@Service
public class JwtService {

    private final SecretKey khoa;
    private final long thoiHanMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long thoiHanMs) {
        this.khoa = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.thoiHanMs = thoiHanMs;
    }

    public String taoToken(NguoiDungPrincipal nguoiDung) {
        Date bayGio = new Date();
        return Jwts.builder()
                .subject(nguoiDung.getEmail())
                .claim("id", nguoiDung.getId())
                .claim("vaiTro", nguoiDung.getAuthorities().iterator().next().getAuthority())
                .issuedAt(bayGio)
                .expiration(new Date(bayGio.getTime() + thoiHanMs))
                .signWith(khoa)
                .compact();
    }

    /** Trả về email trong token; ném JwtException nếu token sai chữ ký hoặc hết hạn. */
    public String layEmail(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(khoa)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public long getThoiHanMs() {
        return thoiHanMs;
    }
}

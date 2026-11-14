package com.techgear.chung.baomat;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Bảo mật: API không trạng thái, xác thực bằng JWT (HS256) qua OAuth2 Resource Server.
 * Đường dẫn công khai của từng module khai báo trong CONG_KHAI_GET / CONG_KHAI_POST.
 */
@Configuration
@EnableMethodSecurity
public class BaoMatConfig {

    /** GET không cần đăng nhập. */
    static final String[] CONG_KHAI_GET = {
            "/uploads/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };
    /** POST không cần đăng nhập. */
    static final String[] CONG_KHAI_POST = {
            "/api/auth/dang-ky", "/api/auth/dang-nhap", "/api/auth/quen-mat-khau", "/api/auth/dat-lai-mat-khau"
    };

    private final SecretKey khoaJwt;

    public BaoMatConfig(JwtProperties properties) {
        this.khoaJwt = new SecretKeySpec(properties.khoaBiMat().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder.withSecretKey(khoaJwt).algorithm(MacAlgorithm.HS256).build();
    }

    /** Kiểm tra mặc định (hạn, nhà phát hành) + các kiểm tra của module (VD thu hồi token ở taikhoan). */
    @Bean
    JwtDecoder jwtDecoder(ObjectProvider<OAuth2TokenValidator<Jwt>> kiemTraThem) {
        List<OAuth2TokenValidator<Jwt>> kiemTra = new ArrayList<>();
        kiemTra.add(JwtValidators.createDefaultWithIssuer(JwtService.NHA_PHAT_HANH));
        kiemTraThem.orderedStream().forEach(kiemTra::add);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(khoaJwt).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(kiemTra));
        return decoder;
    }

    /**
     * Đường dẫn công khai bỏ qua header Authorization: token cũ còn trong trình duyệt
     * không làm hỏng đăng nhập hay xem sản phẩm (frontend luôn gắn token nếu có).
     */
    @Bean
    BearerTokenResolver bearerTokenResolver() {
        List<RequestMatcher> ds = new ArrayList<>();
        Arrays.stream(CONG_KHAI_GET).forEach(p -> ds.add(PathPatternRequestMatcher.pathPattern(HttpMethod.GET, p)));
        Arrays.stream(CONG_KHAI_POST).forEach(p -> ds.add(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, p)));
        RequestMatcher congKhai = new OrRequestMatcher(ds);
        DefaultBearerTokenResolver macDinh = new DefaultBearerTokenResolver();
        return request -> congKhai.matches(request) ? null : macDinh.resolve(request);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain chuoiBaoMat(HttpSecurity http, JsonLoiBaoMat jsonLoi) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API không dùng cookie phiên, token gửi qua header
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, CONG_KHAI_GET).permitAll()
                        .requestMatchers(HttpMethod.POST, CONG_KHAI_POST).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("QUAN_TRI")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(chuyenDoiVaiTro()))
                        .authenticationEntryPoint(jsonLoi))
                .exceptionHandling(e -> e.authenticationEntryPoint(jsonLoi).accessDeniedHandler(jsonLoi));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String origins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    /** Claim vaiTro (quan_tri | khach_hang) → quyền ROLE_QUAN_TRI | ROLE_KHACH_HANG. */
    static JwtAuthenticationConverter chuyenDoiVaiTro() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String vaiTro = jwt.getClaimAsString(JwtService.CLAIM_VAI_TRO);
            Collection<GrantedAuthority> quyen = vaiTro == null ? List.of()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro.toUpperCase(Locale.ROOT)));
            return quyen;
        });
        return converter;
    }
}

package com.techgear.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI tại /swagger-ui.html; bấm "Authorize" và dán token (không cần chữ Bearer) để gọi API cần đăng nhập. */
@Configuration
public class OpenApiConfig {

    private static final String JWT = "jwt";

    @Bean
    public OpenAPI techGearOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechGear API")
                        .version("1.0.0")
                        .description("REST API website thương mại điện tử bán phụ kiện công nghệ TechGear. "
                                + "Lỗi trả về dạng { thoiGian, trangThai, thongBao, loi }."))
                .components(new Components().addSecuritySchemes(JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(JWT));
    }
}

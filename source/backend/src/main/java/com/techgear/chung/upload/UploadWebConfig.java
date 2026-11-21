package com.techgear.chung.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Phục vụ ảnh đã upload tại /uploads/** (công khai). */
@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

    private final String thuMuc;

    public UploadWebConfig(@Value("${app.upload.thu-muc}") String thuMuc) {
        this.thuMuc = thuMuc;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(LuuAnhService.TIEN_TO + "**")
                .addResourceLocations(LuuAnhService.thuMucGoc(thuMuc).toUri().toString());
    }
}

package com.techgear.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/** Cho phép truy cập ảnh đã upload qua đường dẫn /uploads/** */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String viTri = Path.of(uploadDir).toAbsolutePath().toUri().toString();
        if (!viTri.endsWith("/")) {
            viTri += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(viTri);
    }
}

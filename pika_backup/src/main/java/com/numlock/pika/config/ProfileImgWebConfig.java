package com.numlock.pika.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class ProfileImgWebConfig implements WebMvcConfigurer {


    private final String classpathResourcePath = "classpath:/static/profile/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String uploadImagesAbsPath = Paths.get(System.getProperty("user.home"),
                "pika_uploads", "profile")
                .toAbsolutePath().toString().replace("||", "/") + "/";

        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:"  + uploadImagesAbsPath)
                .addResourceLocations(classpathResourcePath);

    }
}

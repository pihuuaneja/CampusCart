package com.campuscart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.campuscart.service.FileStorageService;

/** Serves uploaded photos at /uploads/**. */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final FileStorageService storage;

    public WebConfig(FileStorageService storage) { this.storage = storage; }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(storage.root().toUri().toString())
                .setCachePeriod(86400);
    }
}

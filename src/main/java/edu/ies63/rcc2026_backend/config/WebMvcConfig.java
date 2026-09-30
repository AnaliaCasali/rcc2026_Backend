package edu.ies63.rcc2026_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/perfiles/**")
        .addResourceLocations("file:uploads/perfiles/");

    registry.addResourceHandler("/uploads/posts/**")
        .addResourceLocations("file:uploads/posts/");
  }
}
package com.h.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Web MVC配置类
 * @author hy
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final static List<String> EXCLUDE_PATTERNS = Arrays.asList(
            "/login",
            "/logout",
            "/nacos/**",
            "/nacos-http/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs/**",
            "/swagger-ui/**",
            "/favicon.ico",
            "/admin/**"
    );

    private final static List<String> INCLUDE_PATTERNS = Arrays.asList(
            "/jobInfo/**",
            "/serverAddress/**",
            "/jobLog/**",
            "/user/**"
    );
    @Resource
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns(INCLUDE_PATTERNS)
                .excludePathPatterns(EXCLUDE_PATTERNS);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射前端资源
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/admin/")
                .setCachePeriod(0);

    }
}
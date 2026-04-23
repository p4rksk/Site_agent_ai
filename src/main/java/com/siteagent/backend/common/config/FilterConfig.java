package com.siteagent.backend.common.config;

import com.siteagent.backend.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(jwtAuthenticationFilter);
        bean.addUrlPatterns(    "/admin/sites/*",
        "/admin/chatlogs",
        "/admin/profile",
        "/sites/*",
        "/chat/*"); 
        return bean;
    }
}
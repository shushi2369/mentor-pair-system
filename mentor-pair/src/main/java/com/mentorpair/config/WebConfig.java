package com.mentorpair.config;

import com.mentorpair.interceptor.LoginInterceptor;
import com.mentorpair.interceptor.RoleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/403", "/error", "/css/**", "/js/**", "/favicon.ico");
        registry.addInterceptor(new RoleInterceptor())
                .addPathPatterns("/admin/**", "/mentor/**", "/student/**");
    }
}

package com.barmao.task.manager.config;

import io.hawt.web.auth.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HawtioConfig {
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> hawtioAuthenticationFilter() {
        FilterRegistrationBean<AuthenticationFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new AuthenticationFilter());
        filter.addInitParameter("hawtio.authenticationEnabled", "false");
        filter.addUrlPatterns("/hawtio/*");
        return filter;
    }
}
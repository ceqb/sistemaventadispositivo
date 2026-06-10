package com.ceqb.SistemaVentaDispositivos2025.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true); // 👈 loguea el body
        filter.setMaxPayloadLength(10000); // tamaño máximo del body
        filter.setIncludeHeaders(true); // incluye headers
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }
}

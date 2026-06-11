
package com.ceqb.SistemaVentaDispositivos2025.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ Inyectamos el LoginInterceptor aquí
    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    // ✅ Este método es el que faltaba para registrar el interceptor
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // Intercepta todas las URL
                .excludePathPatterns(
                        "/login",
                        "/uploads/**",
                        "/registrar",
                        "/verificar",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/**/favicon.ico",

                        // 🔑 QR ENTREGA
                        "/pedidosRepartidor/confirmar-entrega",

                        "/desbloquearUsuario",
                        "/reenviar-codigo",
                        "/tienda",
                        "/tienda/**",
                        "/test-notificacion",
                        "/ws",
                        "/ws/**",
                        "/ws-notificaciones",
                        "/ws-notificaciones/**",

                        // AdminLTE (TU ESTRUCTURA REAL)
                        "/AdminLTE/**",
                        "/AdminLTE/dist/**",
                        "/AdminLTE/custom/**",
                        "/AdminLTE/plugins/**",

                        // Subcarpetas específicas
                        "/AdminLTE/dist/img/**",
                        "/AdminLTE/dist/css/**",
                        "/AdminLTE/dist/js/**"
                );
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");
        // AdminLTE
        registry.addResourceHandler("/AdminLTE/**")
                .addResourceLocations("classpath:/static/AdminLTE/");
    }
    @Bean
       public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
           return new HiddenHttpMethodFilter();
       }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


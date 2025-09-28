package com.ragchat.rag_chat_storage.config;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        if ("*".equals(allowedOrigins)) {
            corsConfig.addAllowedOriginPattern("*");
        } else {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            corsConfig.setAllowedOrigins(origins);
        }

        List<String> methods = Arrays.asList(allowedMethods.split(","));
        corsConfig.setAllowedMethods(methods);

        if ("*".equals(allowedHeaders)) {
            corsConfig.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            corsConfig.setAllowedHeaders(headers);
        }

        corsConfig.setAllowCredentials(allowCredentials);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
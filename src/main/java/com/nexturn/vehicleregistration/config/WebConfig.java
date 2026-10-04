package com.nexturn.vehicleregistration.config;

import com.nexturn.vehicleregistration.exception.CorsConfigurationException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(
            @Value("${vrs.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
            String configuredOrigins) {

        allowedOrigins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toArray(String[]::new);

        if (allowedOrigins.length == 0
                || Arrays.stream(allowedOrigins).anyMatch(origin -> !validOrigin(origin))) {
            throw new CorsConfigurationException(
                    "CORS requires exact HTTP or HTTPS frontend origins");
        }
    }

    private boolean validOrigin(String origin) {
        try {
            URI uri = URI.create(origin);
            return ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                    && uri.getHost() != null
                    && uri.getUserInfo() == null
                    && (uri.getRawPath() == null || uri.getRawPath().isEmpty())
                    && uri.getQuery() == null
                    && uri.getFragment() == null
                    && !origin.contains("*");
        } catch (IllegalArgumentException invalidOrigin) {
            return false;
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Accept", "Content-Type", "Authorization", "X-Requested-With")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ApiRequestArgumentResolver());
    }
}

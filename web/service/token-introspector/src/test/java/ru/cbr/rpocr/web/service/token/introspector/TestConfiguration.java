package ru.cbr.rpocr.web.service.token.introspector;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfiguration {

    @Bean
    @ConfigurationProperties("security.oauth2")
    public SecurityOauth2Config getSecurityOauth2Config() {
        return new SecurityOauth2Config();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    static class SecurityOauth2Config {
        private String token;
    }
}

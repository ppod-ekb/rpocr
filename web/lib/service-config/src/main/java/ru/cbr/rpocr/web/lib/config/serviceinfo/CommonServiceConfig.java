package ru.cbr.rpocr.web.lib.config.serviceinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("service")
public class CommonServiceConfig {

    @Getter @Setter
    private String version;
    @Getter @Setter
    private ServiceApi api;

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceApi {

        @Getter @Setter
        private String version;
    }
}

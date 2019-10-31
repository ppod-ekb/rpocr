package ru.cbr.rpocr.web.service.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import ru.cbr.rpocr.web.lib.common.VersionController;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @RestController
    static class AppVersionController extends VersionController.VersionControllerImpl {

        public AppVersionController(@Autowired AppServiceConfig config) {
            super(() -> config.getVersion());
        }
    }

    @Configuration
    @ConfigurationProperties("service")
    static class AppServiceConfig {
        private String version;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}

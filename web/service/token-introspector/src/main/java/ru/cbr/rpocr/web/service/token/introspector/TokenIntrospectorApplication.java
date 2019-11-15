package ru.cbr.rpocr.web.service.token.introspector;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import ru.cbr.rpocr.web.lib.config.serviceinfo.EnableRpocrServiceInfo;

@EnableRpocrServiceInfo
@SpringBootApplication
public class TokenIntrospectorApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .main(TokenIntrospectorApplication.class)
                .sources(TokenIntrospectorApplication.class)
                .profiles("prod")
                .run(args);
    }
}

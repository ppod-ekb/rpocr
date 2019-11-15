package ru.cbr.rpocr.web.service.crl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import ru.cbr.rpocr.web.lib.config.serviceinfo.EnableRpocrServiceInfo;

@EnableRpocrServiceInfo
@SpringBootApplication
public class CalcLauncherApplication {
    private static final Logger console = LoggerFactory.getLogger(CalcLauncherApplication.class);

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .main(CalcLauncherApplication.class)
                .sources(CalcLauncherApplication.class)
                .profiles("prod")
                .run(args);
    }


}

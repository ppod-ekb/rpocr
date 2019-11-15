package ru.cbr.rpocr.web.service.crl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class ApplicationConfig {

    @Bean
    ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(1);
    }

    @Bean
    @Scope("prototype")
    @ConfigurationProperties("spark")
    SparkLauncher getSparkLauncher() {
        return new SparkLauncher();
    }

    @Bean
    @ConfigurationProperties("spark")
    SparkConfig getSparkConfig() {
        SparkConfig config = new SparkConfig();
        return config;
    }

    @Getter @Setter
    static class SparkConfig {
        private String master;
        private String redirectOutputFileName;

    }
}

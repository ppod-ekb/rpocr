package ru.cbr.rpocr.web.service.crl;

import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import ru.cbr.rpocr.web.lib.common.VersionController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
public class Application {
    private static final Logger console = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @RestController
    static class AppVersionController extends VersionController.VersionControllerImpl {

        public AppVersionController(@Autowired AppServiceConfig config) {
            super(() -> config.getVersion());
        }
    }

    @Service
    static class CalcRateLauncherExecutorService {

        private final CalcRateLauncher launcher;
        private final ExecutorService executor;

        public CalcRateLauncherExecutorService(@Autowired ExecutorService executor,
                                               @Autowired CalcRateLauncher launcher) {
            this.executor =  executor;
            this.launcher = launcher;
        }

        public Future process() {
            return executor.submit(launcher);
        }
    }

    @Bean
    ExecutorService getExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    SparkLauncher getSparkLauncher(@Autowired ContainerConfig config) {
        return new SparkLauncher()
                .setSparkHome(config.getSparkHome())
                .setJavaHome(config.getJavaHome())
                .setAppResource(config.getDriverLocation())
                .setMainClass(config.getDriverMainClass())
                .setMaster(config.getSparkMaster());
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

    @Component
    static class CalcRateLauncher implements Runnable {
        private final SparkLauncher launcher;

        public CalcRateLauncher(@Autowired SparkLauncher launcher) {
            this.launcher = launcher;
            this.launcher.setVerbose(true);
        }

        public void run() {
            try {
                Files.deleteIfExists(Paths.get("/tmp/spark-launcher.out.log"));
                launcher.redirectOutput(Files.createFile(Paths.get("/tmp/spark-launcher.out.log")).toFile());
                SparkAppHandle handle = launcher.startApplication();

                while(!handle.getState().isFinal()) {
                    Thread.yield();
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Configuration
    @ConfigurationProperties("container")
    static class ContainerConfig {
        private String sparkHome;
        private String javaHome;
        private String driverLocation;
        private String driverMainClass;
        private String sparkMaster;

        public String getSparkHome() {
            return sparkHome;
        }

        public void setSparkHome(String sparkHome) {
            this.sparkHome = sparkHome;
        }

        public String getJavaHome() {
            return javaHome;
        }

        public void setJavaHome(String javaHome) {
            this.javaHome = javaHome;
        }

        public String getDriverLocation() {
            return driverLocation;
        }

        public void setDriverLocation(String driverLocation) {
            this.driverLocation = driverLocation;
        }

        public String getDriverMainClass() {
            return driverMainClass;
        }

        public void setDriverMainClass(String driverMainClass) {
            this.driverMainClass = driverMainClass;
        }

        public String getSparkMaster() {
            return sparkMaster;
        }

        public void setSparkMaster(String sparkMaster) {
            this.sparkMaster = sparkMaster;
        }
    }
}

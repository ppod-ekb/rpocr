package ru.cbr.rpocr.web.service.crl;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkLauncher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


@RunWith(Enclosed.class)
public class ApplicationIntegrationTest {

    @Slf4j
    @RunWith(SpringRunner.class)
    @SpringBootTest(
            classes = {CalcLauncherApplication.class, ApplicationConfig.class},
            webEnvironment = SpringBootTest.WebEnvironment.NONE)
    public static class SparkTaskTest {

        @Autowired
        SparkLauncher sparkLauncher;

        @Value("${spark.master}")
        String sparkMaster;

        String redirectOuptutFilePath = "/tmp/spark-launcher.out.log";

        @Before
        public void clear() throws IOException {
            log.debug(">>> clear templorary folder: " + redirectOuptutFilePath);
            Files.deleteIfExists(Paths.get(redirectOuptutFilePath));
            Files.createFile(Paths.get(redirectOuptutFilePath));

        }

        @Test
        public void runSparkTaskTest() {
            log.debug(">>> run calc rate launcher test");

            SparkTask.MainIndicatorsOfRubleExchangeRateTaskSpec taskSpec = new SparkTask.MainIndicatorsOfRubleExchangeRateTaskSpec(sparkLauncher);

            taskSpec.sparkMaster(sparkMaster)
                    .redirectOutputFilePath(redirectOuptutFilePath)
                    .priceIndexesUrl("/mnt/spark/volume/price_index_by_month.json")
                    .currencyRateUrl("/mnt/spark/volume/currency_rate.json")
                    .exportImportTurnoverUrl("/mnt/spark/volume/export_import.json");

            SparkTask sparkTask = taskSpec.build();
                    sparkTask.run();
        }
    }


    @Slf4j
    @RunWith(SpringRunner.class)
    @TestPropertySource(locations="/application-test.yml")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                    classes = {CalcLauncherApplication.class, ApplicationConfig.class})
    static public class SparkTaskExecutorServiceIntegrationTest {

        @Autowired
        SparkTaskExecutorService sparkTaskExecutorService;

        @Value("${spark.master}")
        String sparkMaster;

        @Autowired
        SparkLauncher sparkLauncher;

        String redirectOuptutFilePath = "/tmp/spark-launcher.out.log";

        @Before
        public void clear() throws IOException {
            log.debug(">>> clear templorary folder: " + redirectOuptutFilePath);
            Files.deleteIfExists(Paths.get(redirectOuptutFilePath));
            Files.createFile(Paths.get(redirectOuptutFilePath));
        }

        @Test
        public void serviceTest() throws ExecutionException, InterruptedException {
            log.debug(">>> run launcher executor test");

            SparkTask.MainIndicatorsOfRubleExchangeRateTaskSpec taskSpec = new SparkTask.MainIndicatorsOfRubleExchangeRateTaskSpec(sparkLauncher);

            taskSpec.sparkMaster(sparkMaster)
                    .redirectOutputFilePath(redirectOuptutFilePath)
                    .priceIndexesUrl("/mnt/spark/volume/price_index_by_month.json")
                    .currencyRateUrl("/mnt/spark/volume/currency_rate.json")
                    .exportImportTurnoverUrl("/mnt/spark/volume/export_import.json");

            SparkTask sparkTask = taskSpec.build();

            SparkTaskExecutorService.SparkTaskExecution<Void> execution = sparkTaskExecutorService.execute(sparkTask);

            execution.getCompletableFuture().thenAccept(c -> Assert.assertNull(c));
            execution.getFuture().get();
        }
    }

}

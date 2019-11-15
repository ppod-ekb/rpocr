package ru.cbr.rpocr.web.service.crl;

import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

class SparkTask implements Runnable {

    private final SparkLauncher launcher;

    private SparkTask(SparkLauncher launcher) {
        this.launcher = launcher;
    }

    public void run() {
        try {
            SparkAppHandle handle = launcher.startApplication();
            while(!handle.getState().isFinal()) {
                Thread.yield();
            }
        } catch (IOException e) {
            throw new SparkTaskExecutionRuntimeException(e.getMessage(), e);
        }
    }

    public static class SparkTaskExecutionRuntimeException extends RuntimeException {
        public SparkTaskExecutionRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MainIndicatorsOfRubleExchangeRateTaskSpec extends SparkTaskSpec<MainIndicatorsOfRubleExchangeRateTaskSpec> {
        private String priceIndexesUrl;
        private String exportImportTurnoverUrl;
        private String currencyRateUrl;

        public MainIndicatorsOfRubleExchangeRateTaskSpec(SparkLauncher sparkLauncher) {
            super(sparkLauncher);
        }

        @Override
        protected MainIndicatorsOfRubleExchangeRateTaskSpec self() {
            return this;
        }

        public SparkTask build() {
            Assert.notNull(priceIndexesUrl, "specify price index location");
            Assert.notNull(exportImportTurnoverUrl, "specify export/import turnover location");
            Assert.notNull(currencyRateUrl, "specify currency rate location");

            sparkLauncher.addAppArgs(priceIndexesUrl);
            sparkLauncher.addAppArgs(exportImportTurnoverUrl);
            sparkLauncher.addAppArgs(currencyRateUrl);
            return super.build();
        }

        public MainIndicatorsOfRubleExchangeRateTaskSpec priceIndexesUrl(String priceIndexesUrl) {
            this.priceIndexesUrl = priceIndexesUrl;
            return self();
        }

        public MainIndicatorsOfRubleExchangeRateTaskSpec exportImportTurnoverUrl(String exportImportTurnoverUrl) {
            this.exportImportTurnoverUrl = exportImportTurnoverUrl;
            return self();
        }

        public MainIndicatorsOfRubleExchangeRateTaskSpec currencyRateUrl(String currencyRateUrl) {
            this.currencyRateUrl = currencyRateUrl;
            return self();
        }
    }

    public static abstract class SparkTaskSpec<T extends SparkTaskSpec> {

        protected final SparkLauncher sparkLauncher;

        public SparkTaskSpec(SparkLauncher sparkLauncher) {
            this.sparkLauncher = sparkLauncher;
        }

        public SparkLauncher withSparkLauncher() {
            return sparkLauncher;
        }

        public T sparkMaster(String sparkMaster) {
            sparkLauncher.addAppArgs(sparkMaster);
            return self();
        }

        public T redirectOutputFilePath(String redirectOutputFilePath) {
           sparkLauncher.redirectOutput(new File(redirectOutputFilePath));
           return self();
        }

        protected abstract T self();

        public SparkTask build() {
            return new SparkTask(sparkLauncher);
        }

    }
}

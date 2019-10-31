package ru.cbr.rpocr.web.service.crl;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


@RunWith(Enclosed.class)
public class ApplicationIntegrationTest {
    private static final Logger console = LoggerFactory.getLogger(ApplicationIntegrationTest.class);

    @RunWith(SpringRunner.class)
    @SpringBootTest(
            classes = Application.class,
            webEnvironment = SpringBootTest.WebEnvironment.NONE)
    public static class CalcRateLauncherTest {

        @Autowired
        private Application.CalcRateLauncher launcher;

        @Test
        public void testTest() {
            console.debug("run calc rate launcher test");
            launcher.run();
        }
    }


    @RunWith(SpringRunner.class)
    @SpringBootTest(
            classes = Application.class,
            webEnvironment = SpringBootTest.WebEnvironment.NONE)
    static public class CalcRateLauncherExecutorServiceIntegrationTest {

        @Autowired Application.CalcRateLauncherExecutorService service;
        @Autowired
        ExecutorService executor;

        @Test
        public void serviceTest() throws ExecutionException, InterruptedException {
            console.debug("run launcher executor test");
            service.process().get();
            executor.shutdown();
        }
    }

}

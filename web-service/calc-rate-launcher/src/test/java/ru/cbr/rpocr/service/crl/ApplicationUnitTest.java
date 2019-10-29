package ru.cbr.rpocr.service.crl;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Enclosed.class)
public class ApplicationUnitTest {
    private static final Logger console = LoggerFactory.getLogger(ApplicationUnitTest.class);

    public static class TestOfInnerClass {
        @Test
        public void test() {
            console.debug("run unit test");
        }
    }
}

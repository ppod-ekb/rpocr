package ru.cbr.rpocr.spark.crd;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PtCalcIntegrationTest {

    private static final String JSON_INDEX_DS_FILE_PATH = "/mnt/spark/volume/price_index_by_month.json";
    private static final Logger console = LoggerFactory.getLogger(PtCalcIntegrationTest.class);

    private SparkConf sparkConfig() {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[*]")
                //conf.setMaster("spark://172.17.0.5:7077")
                //conf.setMaster("spark://localhost:7077")
                .set("spark.ui.enabled", "false")
                .setAppName("SparkMe App");
        return conf;
    }

    private SparkSession sparkSession() {
        SparkContext sparkContext = SparkContext.getOrCreate(sparkConfig());
        SparkSession spark = SparkSession
                .builder()
                //.sparkContext(sparkContext)
                //.master("spark://172.17.0.5:7077")
                //.master("172.17.0.5:7077")
                .config(sparkConfig())
                //.enableHiveSupport()
                .appName("Java Spark SQL basic example")
                .getOrCreate();
        return spark;
    }

    @Test
    @Disabled
    public void calculatePtTest() {
        /*LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // print logback's internal status
        StatusPrinter.print(lc);*/
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
        console.info("calculate pt test");
        PtCalc ptCalc = new PtCalc(sparkSession(), JSON_INDEX_DS_FILE_PATH);
        ptCalc.calculate();
    }
}

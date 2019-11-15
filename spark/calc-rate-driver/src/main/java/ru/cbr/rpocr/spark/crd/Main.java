package ru.cbr.rpocr.spark.crd;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final Logger console = LoggerFactory.getLogger(Main.class);
    private static final Properties PROPS = new PropsConfigLoader("application.properties").load().getProps();

    private static class PropsConfigLoader {

        private final String propertiesFileName;
        private final Properties props;

        public PropsConfigLoader(String propertiesFileName) {
            this.propertiesFileName = propertiesFileName;
            props = new Properties();
        }

        public PropsConfigLoader load() {
            try {
                props.load(this.getClass().getClassLoader().getResourceAsStream(propertiesFileName));
                return this;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(),e);
            }

        }

        public Properties getProps() {
            return props;
        }
    }

    private static SparkConf sparkConfig(String sparkMaster) {
        SparkConf conf = new SparkConf();
        //conf.setMaster(PROPS.getProperty("spark.mster"))
        conf.setMaster(sparkMaster)
        //conf.setMaster("local[*]")
        //conf.setMaster("spark://172.17.0.5:7077")
        .set("spark.ui.enabled", "false")
        //.set("spark.eventLog.enabled ", "true")
        ;
        return conf;
    }

    private static SparkSession sparkSession(String sparkMaster) {
        SparkSession spark = SparkSession
                .builder()
                .config(sparkConfig(sparkMaster))
                .appName(PROPS.getProperty("application.name"))
                .getOrCreate();
        return spark;
    }

    public static void main(String[] args){
        console.info(">>> start calculate indexes: " + args.length);
        console.info(">>> master: " + args[0]);
        console.info(">>> data: " + args[1]);

        String sparkMasterLocation = args[0];
        String priceIndexesJsonlocation = args[1];

        SparkSession spark = sparkSession(sparkMasterLocation);
        PqCalc pqCalc = new PqCalc(spark, PROPS.getProperty("data.priceIndexesJson"));
        //PtCalc ptCalc = new PtCalc(spark, PROPS.getProperty("data.priceIndexesJson"));

        pqCalc.calculate();
    }
}

package ru.cbr.rpocr.spark.crd;

import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.api.java.UDF6;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.reflect.ClassTag$;

import static org.apache.spark.sql.functions.*;

public class PqCalc {
    private static final Logger console = LoggerFactory.getLogger(PqCalc.class);

    private final SparkSession spark;
    private final String jsonIndexDsAbsoluteFilePath;

    public PqCalc(SparkSession spark, String jsonIndexDsAbsoluteFilePath) {
        this.spark = spark;
        this.jsonIndexDsAbsoluteFilePath = jsonIndexDsAbsoluteFilePath;

        register_yearmo_func(spark);
        register_quart_func(spark);
        register_monthquart_func(spark);
        register_plus_int_func(spark);
        register_calc_pq_func(spark);
    }

    private StructType priceIndexByMonthStructType() {
        return new StructType()
                .add("country", DataTypes.StringType, false)
                .add("year", DataTypes.IntegerType, false)
                .add("month", DataTypes.IntegerType, false)
                .add("monthName", DataTypes.StringType, false)
                .add("index", DataTypes.DoubleType, false);
    }

    private Dataset<Row> getIndexeDs() {
        return spark.read().schema(priceIndexByMonthStructType()).json(jsonIndexDsAbsoluteFilePath);
    }

    public void calculate() {
        console.debug("calculate Pq indexes");
        Dataset<Row> indexDs = getIndexeDs();
        indexDs.printSchema();
        //indexDs.show(20);

        Column[] orderedColumns = new Column[]{col("country"), col("year"), col("quart"), col("monthquart"), col("quartmo"), col("index")};

        Dataset<Row> preparedIndexDs = indexDs.withColumn("yearmo", callUDF("YEARMO", col("year"), col("month")))
                .withColumn("monthquart", callUDF("MONTHQUART", col("month")))
                .withColumn("quart", callUDF("QUART", col("month")))
                .withColumn("quartmo", concat(col("quart"), col("monthquart")))
                .select(orderedColumns)
                ;
        //preparedIndexDs.show(20);

        Dataset<Row> decemberIndexes = preparedIndexDs.select("country", "monthquart", "year", "index")
                .where(col("quart").equalTo(4))
                .withColumn("year", callUDF("PLUSINT",col("year"), lit(1)))
                .withColumn("quart", lit(0))
                .withColumn("quartmo", concat(col("quart"), col("monthquart")))
                .select(orderedColumns)
                ;
        //decemberIndexes.show(20);

        // add 4-th quartal to base ds
        Dataset<Row> preparedIndexesWithDecemberIndexes = preparedIndexDs.union(decemberIndexes);
        //preparedIndexesWithDecemberIndexes.orderBy("country", "year", "quartmo").show(20);

        // pivot by quartmo [01 02 03 11 12 13 21 22 23 31 32 33 41 42 43]
        Dataset<Row> quartMonthIndexes = preparedIndexesWithDecemberIndexes.groupBy("country", "year")
                .pivot("quartmo")
                .min("index")
                .na().fill(0)
                ;

        //quartMonthIndexes.orderBy("country", "year").show(100);

        // final calculate quart indexes
        Dataset<Row> pqIndexes = quartMonthIndexes.withColumn("q1", callUDF("CALCPQ",
                col("01"), col("02"), col("03"),
                col("11"), col("12"), col("13")))
                .withColumn("q2", callUDF("CALCPQ",
                        col("11"), col("12"), col("13"),
                        col("21"), col("22"), col("23")))
                .withColumn("q3", callUDF("CALCPQ",
                        col("21"), col("22"), col("23"),
                        col("31"), col("32"), col("33")))
                .withColumn("q4", callUDF("CALCPQ",
                        col("31"), col("32"), col("33"),
                        col("41"), col("42"), col("43")))
                .select("year", "country", "q1", "q2", "q3", "q4")
                ;
        pqIndexes.orderBy("country", "year").show(100);
    }

    private void register_yearmo_func(SparkSession spark) {
        spark.udf().register("YEARMO",(UDF2<Integer, Integer, String>)
                        (year, mo) -> {
                            if (mo < 10) {
                                return year + "0" + mo;
                            } else {
                                return year + "" + mo;
                            }
                        },
                DataTypes.StringType);
    }

    private void register_quart_func(SparkSession spark) {
        spark.udf().register("QUART",(UDF1<Integer, Integer>)
                        (mo) -> {
                            if (mo <= 3) {
                                return 1;
                            }
                            if (mo <= 6) {
                                return 2;
                            }
                            if (mo <= 9) {
                                return 3;
                            }
                            if (mo <= 12) {
                                return 4;
                            }

                            return 0;
                        },
                DataTypes.IntegerType);
    }

    private void register_monthquart_func(SparkSession spark) {
        Broadcast<int[]> broadcastVar = spark.sparkContext().broadcast(new int[]{1,2,3,1,2,3,1,2,3,1,2,3}, ClassTag$.MODULE$.apply(int[].class));

        spark.udf().register("MONTHQUART",(UDF1<Integer, Integer>)
                        (mo) -> {
                            if (mo>=1 && mo <= 12) return broadcastVar.getValue()[mo-1];
                            else return 0;
                        },
                DataTypes.IntegerType);
    }

    private void register_plus_int_func(SparkSession spark) {
        spark.udf().register("PLUSINT",(UDF2<Integer, Integer, Integer>)
                        (v1, v2) -> {
                            return v1+v2;
                        },
                DataTypes.IntegerType);
    }

    private void register_calc_pq_func(SparkSession spark) {
        spark.udf().register("CALCPQ",(UDF6<Double, Double, Double, Double, Double, Double, Double>)
                        (pq0m1, pq0m2, pq0m3, pqm1, pqm2, pqm3) -> {
                            Double divide = pq0m1 * pq0m2 * pq0m3 * (pqm1 + pqm1 * pqm2 + pqm1 * pqm2 * pqm3);
                            Double divider =  (pq0m1 + pq0m1 * pq0m2 + pq0m1 * pq0m2 * pq0m3);

                            if (divider == 0) {
                                return 0.0;
                            } else {
                                return divide / divider;
                            }
                        },
                DataTypes.DoubleType);
    }

}

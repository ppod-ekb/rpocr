package ru.cbr.rpocr.spark.crd;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.api.java.UDF3;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.Iterator;
import scala.collection.mutable.WrappedArray;

import static org.apache.spark.sql.functions.*;

public class PtCalc {

    private static final Logger console = LoggerFactory.getLogger(PtCalc.class);

    private final SparkSession spark;
    private final String jsonIndexDsAbsoluteFilePath;

    public PtCalc(SparkSession spark, String jsonIndexDsAbsoluteFilePath) {
        this.spark = spark;
        this.jsonIndexDsAbsoluteFilePath = jsonIndexDsAbsoluteFilePath;

        register_multiply_array_func(spark);
        register_sum_array_func(spark);
        register_calc_pm_func(spark);
        register_calc_sumpm_func(spark);
        register_calc_pt_func(spark);
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
        return spark.read().schema(priceIndexByMonthStructType()).json(jsonIndexDsAbsoluteFilePath)
                        //.where(col("country").equalTo("Russia"))
                ;
    }

    public void calculate() {
        console.debug("calculate Pt indexes");
        Column[] orderedColumns = new Column[]{col("country"), col("year"), col("month"), col("index")};
        Dataset<Row> indexDs = getIndexeDs();
        indexDs.printSchema();

        Dataset<Row> currIndexes = indexDs.select(orderedColumns);
        Dataset<Row> prevIndexes = indexDs.select("country", "year", "month", "index")
                .withColumn("year", col("year").plus(1))
                .withColumn("month", col("month").multiply(-1)).select(orderedColumns);

        Dataset<Row> unionIndexes = currIndexes.union(prevIndexes);
        //indexDs.select(orderedColumns).show(20);

        // pivot indexes by month
        Dataset<Row> monthIndexesPivotDs = unionIndexes.groupBy("country", "year").pivot("month").mean("index").na().fill(0.0);

        // add array columns prev_month_indexes and curr_month_indexes
        Dataset<Row> monthIndexesArrayPivotDs = monthIndexesPivotDs.withColumn("prev_month_indexes", array("-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10", "-11", "-12"))
                .withColumn("curr_month_indexes", array("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"))
                .select("country", "year", "prev_month_indexes", "curr_month_indexes");

        // calc Pmy, calc Pm indexes by multiply array prev_month_indexes, curr_month_indexes from 1..T
        Dataset<Row> pmIndexesPivotDs = withPmIndexes(monthIndexesArrayPivotDs.withColumn("pmy", callUDF("MULTIPLYARRAY", col("prev_month_indexes"))));

        // add array columns prev_pm_indexes and curr_pm_indexes
        Dataset<Row> pmIndexesArrayPivotDs = pmIndexesPivotDs.withColumn("prev_pm_indexes", array("-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10", "-11", "-12"))
                .withColumn("curr_pm_indexes", array("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"))
                .select("country", "year", "pmy", "prev_pm_indexes", "curr_pm_indexes");


        // calc sum of Pm indexes by sum array prev_pm_indexes and curr_pm_indexes form 1..T
        Dataset<Row> sumPmIndexesPivotDs = withSumOfPmIndexes(pmIndexesArrayPivotDs);

        // final calc pt1
        Dataset<Row> ptIndexes = sumPmIndexesPivotDs.select(col("country"), col("year"),
                callUDF("CALCPT", col("pmy"), col("1"), col("-1")).as("pt1"),
                callUDF("CALCPT", col("pmy"), col("2"), col("-2")).as("pt2"),
                callUDF("CALCPT", col("pmy"), col("3"), col("-3")).as("pt3"),
                callUDF("CALCPT", col("pmy"), col("4"), col("-4")).as("pt4"),
                callUDF("CALCPT", col("pmy"), col("5"), col("-5")).as("pt5"),
                callUDF("CALCPT", col("pmy"), col("6"), col("-6")).as("pt6"),
                callUDF("CALCPT", col("pmy"), col("7"), col("-7")).as("pt7"),
                callUDF("CALCPT", col("pmy"), col("8"), col("-8")).as("pt8"),
                callUDF("CALCPT", col("pmy"), col("9"), col("-9")).as("pt9"),
                callUDF("CALCPT", col("pmy"), col("10"), col("-10")).as("pt10"),
                callUDF("CALCPT", col("pmy"), col("11"), col("-11")).as("pt11"),
                callUDF("CALCPT", col("pmy"), col("12"), col("-12")).as("pt12")
        );

        ptIndexes.orderBy("country", "year").show(20);
        //indexDsPivot.show(20);
    }

    private Dataset<Row> withPmIndexes(Dataset<Row> monthIndexesArrayPivotDs) {
        for (int i = 1; i <= 12; i++) {
            monthIndexesArrayPivotDs = monthIndexesArrayPivotDs
                    .withColumn(String.valueOf(i*(-1)), callUDF("CALCPM", col("prev_month_indexes"), lit(i)))
                    .withColumn(String.valueOf(i), callUDF("CALCPM", col("curr_month_indexes"), lit(i)));
        }
        return monthIndexesArrayPivotDs;
    }

    private Dataset<Row> withSumOfPmIndexes(Dataset<Row> pmIndexesArrayPivotDs) {
        for (int i = 1; i <= 12; i++) {
            pmIndexesArrayPivotDs = pmIndexesArrayPivotDs.withColumn(String.valueOf(i*(-1)), callUDF("CALCSUMPM", col("prev_pm_indexes"), lit(i)))
                    .withColumn(String.valueOf(i), callUDF("CALCSUMPM", col("curr_pm_indexes"), lit(i)));
        }
        return pmIndexesArrayPivotDs;
    }

    private void register_multiply_array_func(SparkSession spark) {
        spark.udf().register("MULTIPLYARRAY",(UDF1<WrappedArray<Double>, Double>)
                        (arr) -> {
                            Double multiply = 1.0;
                            Iterator<Double> it = arr.iterator();
                            while (it.hasNext()) {
                                multiply = it.next() * multiply;
                            }
                            return multiply;
                        },
                DataTypes.DoubleType);
    }

    private void register_sum_array_func(SparkSession spark) {
        spark.udf().register("SUMARRAY",(UDF1<WrappedArray<Double>, Double>)
                        (arr) -> {
                            Double sum = 0.0;
                            Iterator<Double> it = arr.iterator();
                            while (it.hasNext()) {
                                sum = it.next() + sum;
                            }
                            return sum;
                        },
                DataTypes.DoubleType);
    }

    private void register_calc_pm_func(SparkSession spark) {
        spark.udf().register("CALCPM",(UDF2<WrappedArray<Double>, Integer, Double>)
                        (arr, month) -> {
                            Double multiply = 1.0;
                            for (int i = 0; i < month; i++) {
                                multiply = arr.apply(i) * multiply;
                            }
                            return multiply;
                        },
                DataTypes.DoubleType);
    }

    private void register_calc_sumpm_func(SparkSession spark) {
        spark.udf().register("CALCSUMPM",(UDF2<WrappedArray<Double>, Integer, Double>)
                        (arr, month) -> {
                            Double sum = 0.0;
                            for (int i = 0; i < month; i++) {
                                sum = arr.apply(i) + sum;
                            }
                            return sum;
                        },
                DataTypes.DoubleType);
    }

    private void register_calc_pt_func(SparkSession spark) {
        spark.udf().register("CALCPT",(UDF3<Double, Double, Double, Double>)
                        (pmy, currSumPm, prevSumPm) -> {
                            if (prevSumPm == 0) {
                                return 0.0;
                            }
                            return (pmy * currSumPm) / prevSumPm;
                        },
                DataTypes.DoubleType);
    }
}

package main.java.com.david.jobs;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;


import java.io.File;
import java.util.List;

public class Sort {
    public static void main(String[] args) {
        // Generator the input data and output to file
        String sparkMasterDef = null;
        String dataFile = "C:\\code\\SparkJobs\\data\\sort2.txt";

        if(args == null || args.length == 0){
            // local
            File hadoopDIR = new File("src/main/resources/hadoop/"); // represent the hadoop directory as a Java file so we can get an absolute path for it
            System.setProperty("hadoop.home.dir", hadoopDIR.getAbsolutePath()); // set the JVM system property so that Spark finds it
            sparkMasterDef = "local[2]"; // default is local mode with two executors
        }else{
            sparkMasterDef = args[0];
            dataFile = args[1];
            System.out.println("spark master:" + sparkMasterDef);
        }

        if (sparkMasterDef==null) sparkMasterDef = "local[4]"; // default is local mode with two executors

        // Create the Spark Configuration
        SparkConf conf = new SparkConf()
                .setAppName("sort")
                .setMaster(sparkMasterDef);

        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> textFile = sc.textFile(dataFile);
        System.out.println("开始时间：" + System.currentTimeMillis());
        //将一列String类型的数据转换成int
        JavaRDD<Integer> map = textFile.map(new Function<String, Integer>() {
            @Override
            public Integer call(String s) throws Exception {
                return Integer.parseInt(s);
            }
        });
        //用sortBy进行排序三个参数（new Function，升序或降序，分区）
        JavaRDD<Integer> sortBy = map.sortBy(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) throws Exception {
                return integer;
            }
        }, false, 3);
        //用take算子取前几个
        List<Integer> take = sortBy.collect();
        System.out.println("执行结束:" + System.currentTimeMillis());

//        for (Integer e : take) {
//            System.out.println(e);
//        }
    }
}



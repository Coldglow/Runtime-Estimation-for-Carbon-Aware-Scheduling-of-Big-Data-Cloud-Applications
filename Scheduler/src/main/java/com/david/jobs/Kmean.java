package main.java.com.david.jobs;

import main.java.com.david.config.KMeansConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.io.File;
import java.util.Arrays;

public class Kmean {
    private static long startTime;
    private static long endTime;

    public static void main(String[] args) {
        // Generator the input data and output to file
        String outPutPathRoot;
        String sparkMasterDef = null;
        int clusterNum = KMeansConfig.NUM_CLUSTERS;

        if(args == null || args.length == 0){
            // local
            File hadoopDIR = new File("resources/hadoop/"); // represent the hadoop directory as a Java file so we can get an absolute path for it
            System.setProperty("hadoop.home.dir", hadoopDIR.getAbsolutePath()); // set the JVM system property so that Spark finds it

            outPutPathRoot = "data/";
            sparkMasterDef = "local[2]"; // default is local mode with two executors
        }else{
            outPutPathRoot = args[0];
            sparkMasterDef = args[1];
            clusterNum = Integer.parseInt(args[2]);
            System.out.println("spark master:" + sparkMasterDef);
        }

        String outPutPath = outPutPathRoot;

        if (sparkMasterDef==null) sparkMasterDef = "local[4]"; // default is local mode with two executors

        String sparkSessionName = "Kmeans"; // give the session a name

        // Create the Spark Configuration
        SparkConf conf = new SparkConf()
                .setAppName(sparkSessionName)
                .setMaster(sparkMasterDef);

        JavaSparkContext javaSparkContext = new JavaSparkContext(conf);

        startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);

        JavaRDD<String> data = javaSparkContext.textFile(outPutPath);
        JavaRDD<Vector> parsedData = data.map(s -> {
            String[] strArray = s.split(" ");
            double[] values = new double[strArray.length];
            for (int i = 0; i < strArray.length; i++) {
                values[i] = Double.parseDouble(strArray[i]);
            }
            return Vectors.dense(values);
        });
        parsedData.cache();

        KMeansModel clusters = new KMeans()
                .setK(clusterNum)
                .setMaxIterations(KMeansConfig.NUM_ITERATION + 1)
                .setEpsilon(0)
                .setSeed(1L)
                .run(parsedData.rdd());

        Vector[] vectors = clusters.clusterCenters();
        System.out.println("the centres are: ");
        for (Vector vector : vectors) {
            System.out.println(Arrays.toString(vector.toArray()));
        }

        endTime = System.currentTimeMillis();
        System.out.println("Ent time: " + endTime);
        long cost = endTime - startTime;
        System.out.println("Using " + ((cost / 1000) / 60.0) + " minutes");
    }
}

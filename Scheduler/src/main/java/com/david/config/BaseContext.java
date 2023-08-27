package main.java.com.david.config;

public class BaseContext {
    // Spark Config, keep same with node range in python program
    public static int[] nodes = {2, 4, 6, 8, 10, 12};
    // pkgWatt means the amount of energy required
    // to perform a particular workload in one hour
    public static double minWatt = 0.97 / 1000;   // idle
    public static double lowWatt = 2.65 / 1000;  // 10%
    public static double mediumWatt = 6.27 / 1000;   // 50%
    public static double maxWatt = 8.49 / 1000;   // 100%
    public static String machine_type = "n2d-standard-2";
    // Job config
    public static String jobName = "K-Means";
    // max runtime
    public static int maxRunTime = 600;
    // deadline, from now
    public static int tolerance = 24;

    // python file path
    public static String filePath = "C:\\code\\estimator\\estimator.py";

    // K-Means Job config
    public static String observations = "200000000";
    public static String features = "5";
    public static String K = "5";

    // Sorts Job config
    public static String dataSize = "20000";

}

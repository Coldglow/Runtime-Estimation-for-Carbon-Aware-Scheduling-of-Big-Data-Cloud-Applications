package main.java.com.david.config;

public final class KMeansConfig {
    public static final int NUM_CLUSTERS = 4;
    public static final int NUM_DATASETS = 40000000;
    public static final int MAX_ITERATIONS = 100;
    public static final int NUM_ITERATION = 10;
    public static final int NUM_ITERATION_PER_STEP = 2;
    public static final int NUM_STEPS = NUM_ITERATION / NUM_ITERATION_PER_STEP;
}

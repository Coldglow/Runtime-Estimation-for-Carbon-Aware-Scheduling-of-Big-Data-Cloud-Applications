
package main.java.com.david.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class GenerateData {

    public static void record(String data, File filePath) {
        // Append file
        try (
                FileWriter fileWriter = new FileWriter(filePath, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            bufferedWriter.newLine();
            bufferedWriter.write(data);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        File path = new File("data/record.csv");

        int id = 26;
        String[] kmeansArgs = new String[4];
        int feature = 5;
        int observation = 200000000;
        int k = 5;
        kmeansArgs[0] = String.valueOf(observation);
        kmeansArgs[1] = String.valueOf(k);
        kmeansArgs[2] = "data/" + "KMeans_" +id + ".csv";
        // feature
        kmeansArgs[3] = String.valueOf(feature);
        KMeansDataGenerator.main(kmeansArgs);
    }
}
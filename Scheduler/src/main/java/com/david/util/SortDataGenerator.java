package main.java.com.david.util;

import java.io.*;
import java.util.Random;

public class SortDataGenerator {
    public static void main(String[] args) {
        Random random = new Random();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("data/sort6.txt", true));
            for (int i = 0; i < 50000000; ++i) {
                out.write(String.valueOf(random.nextInt(50000000)));
                out.newLine();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) throws IOException {
//        BufferedReader in = new BufferedReader(new FileReader("data/sort.txt"));
//        String line;
//        while ((line = in.readLine()) != null) {
//            System.out.println(line);
//        }
//    }
}

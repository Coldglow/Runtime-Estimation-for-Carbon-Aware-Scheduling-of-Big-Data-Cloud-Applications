package main.java.com.david.controller;

import lombok.extern.slf4j.Slf4j;
import main.java.com.david.config.BaseContext;
import main.java.com.david.pojo.Job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@Slf4j
public class JobController {

    public static void estimate(Job job) {
        String[] args = JobController.prepareArgs(job);
        String[] tmp;
        try {
            log.info("Start estimating the runtime.");
            // execute python program
            Process proc = Runtime.getRuntime().exec(args);
            // use input and output stream to get the result
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                tmp = line.split(" ");
                SchedulerController.estimatedTime.add(Integer.parseInt(tmp[0]));
                SchedulerController.estimatedCPULoad.add(Float.parseFloat(tmp[1]));
            }
            in.close();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * init the paras that need to run python application
     * @param job the job that need to execute
     * @return
     */
    public static String[] prepareArgs(Job job) {
        int n = Objects.equals(job.getName(), "K-Means") ? 3 : 1;
        String[] argArr = new String[n + 3];
        argArr[0] = "python";                  // this is a python program
        argArr[1] = BaseContext.filePath;      // file path
        argArr[2] = BaseContext.jobName;
//        argArr[3] = String.valueOf(BaseContext.maxRunTime);

        if (n == 3) {
            argArr[3] = BaseContext.observations;
            argArr[4] = BaseContext.features;
            argArr[5] = BaseContext.K;
        } else {
            argArr[3] = BaseContext.dataSize;
        }

        return argArr;
    }
}

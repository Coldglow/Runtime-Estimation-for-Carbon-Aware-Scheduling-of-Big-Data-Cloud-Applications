package main.java.com.david.util;

import main.java.com.david.config.BaseContext;
import main.java.com.david.pojo.Job;
import main.java.com.david.pojo.KMeans;
import main.java.com.david.pojo.Sorts;

import java.time.LocalDateTime;
import java.util.Objects;

public class JobFactory {
    private static int id = 1;

    public static Job newJob(LocalDateTime deadline, String jobName, String... args) {
        Job job;
        if (Objects.equals(jobName, "K-Means")) {
            job = new KMeans(deadline, args);
        } else {
            job = new Sorts(deadline, args);
        }
        job.setId(id++);
        job.setName(BaseContext.jobName);
        job.setMaxRunTime(BaseContext.maxRunTime);
        return job;
    }
}

package main.java.com.david;

import main.java.com.david.config.BaseContext;
import main.java.com.david.controller.SchedulerController;
import main.java.com.david.pojo.Job;
import main.java.com.david.util.JobFactory;

import java.time.LocalDateTime;


public class JobScheduler {

    public static void main(String[] args) {
        LocalDateTime deadline = LocalDateTime.now().plusHours(BaseContext.tolerance);
        // the order of the paras can not be changed
        Job kmeansJob = JobFactory.newJob(deadline, BaseContext.jobName,
                                         BaseContext.observations,
                                         BaseContext.features, BaseContext.K);
        SchedulerController schedulerController = new SchedulerController();
        schedulerController.schedule(kmeansJob);
    }
}

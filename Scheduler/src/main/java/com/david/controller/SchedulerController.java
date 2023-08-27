package main.java.com.david.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import main.java.com.david.config.BaseContext;
import main.java.com.david.exception.InsufficientTimeException;
import main.java.com.david.pojo.Job;
import main.java.com.david.pojo.TimeSlot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class SchedulerController {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static List<TimeSlot> carbonForecasts = new ArrayList<>(48);
    private static Section chosenSection = new Section();
    private static SimpleRegression simpleRegression = new SimpleRegression();
    public static ArrayList<Integer> estimatedTime = new ArrayList<>(6);
    public static ArrayList<Float> estimatedCPULoad = new ArrayList<>(6);

    public void updateForecast() {
        LocalDateTime now = LocalDateTime.now();
        // YYYY-MM-DDThh:mmZ
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
        String formattedTime = now.format(formatter);

        String url = "https://api.carbonintensity.org.uk/intensity/" + formattedTime + "/fw24h";

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String response = responseEntity.getBody();

        if (!carbonForecasts.isEmpty()) {
            carbonForecasts.clear();
        }

        // parse response to json format and then convert to pojo
        JSONArray jsonArray = (JSONArray) Objects.requireNonNull(JSONObject.parse(response)).get("data");
        for (Object obj : jsonArray) {
            TimeSlot slot = JSON.parseObject(obj.toString(), TimeSlot.class);
            slot.jobList = new ArrayDeque<>();       // initial an empty list
            carbonForecasts.add(slot);   // add the time slot to forecast list
        }

        // modify the first window
        Duration duration = Duration.between(now, carbonForecasts.get(0).from);
        carbonForecasts.get(0).restTime += (int) (duration.toMillis() / 1000);
        carbonForecasts.get(0).from = LocalDateTime.parse(formattedTime, formatter);

        // test code
        for (TimeSlot slot : carbonForecasts) {
            System.out.println(slot);
        }
    }

    public void iniRegression() {
        simpleRegression.addData(0, BaseContext.minWatt);
        simpleRegression.addData(0.1, BaseContext.lowWatt);
        simpleRegression.addData(0.5, BaseContext.mediumWatt);
        simpleRegression.addData(1, BaseContext.maxWatt);
    }

    public void schedule(Job job) {
        // estimate the runtime using Estimator
        JobController.estimate(job);
        // update the forecast
        updateForecast();
        // initial the regression model
        iniRegression();
        // Schedule
        int node = 0;
        double avgWatts;
        for (int i = 0; i < estimatedTime.size(); i++) {
            node = BaseContext.nodes[i];
            avgWatts = BaseContext.minWatt + simpleRegression.predict(estimatedCPULoad.get(i));
            System.out.println("node:" + node + "  load:" + estimatedCPULoad.get(i));
            setStartTime(estimatedTime.get(i), job, node, avgWatts);
        }
        log.info(chosenSection.toString());
    }

    /**
     * get the Section that produce the minimal carbon emissions
     * @param estimated estimated runtime of the job
     * @param job the job
     * @param node scale-out
     * @param avgWatts avgWatt
     */
    public void setStartTime(int estimated, Job job, int node, double avgWatts) {
        // if the estimated runtime is larger than the time duration between now and deadline, throw Exception
        if (LocalDateTime.now().plusSeconds(estimated).isAfter(job.getDeadline())) {
            throw new InsufficientTimeException(2, "Can not finish the job before deadline");
        }

        // the latest start time of the job
        LocalDateTime latestStart = job.getDeadline().minusSeconds(estimated);
        Section curSection = new Section();
        // key idea
        getMinWindow(estimated, latestStart, curSection, node, avgWatts);
        // compare with start executing right now
        double nowIntensity = getIntensity(estimated, node, 0, avgWatts, false);
        double optimization = (nowIntensity - curSection.intensity) / nowIntensity * 100;
        log.info("Estimated Runtime:{}s with scale-out:{}. " +
                        "Start time:{}, end time:{}, produce {}g carbon, " +
                        "Start now: {}g carbon will produced, " +
                        "{}% reduction",
                estimated, node,curSection.startTime, curSection.endTime, String.format("%.2f", curSection.intensity),
                String.format("%.2f", nowIntensity), String.format("%.2f", optimization));
        if (curSection.intensity < chosenSection.intensity) {
            chosenSection.intensity = curSection.intensity;
            chosenSection.startTime = curSection.startTime;
            chosenSection.endTime = curSection.endTime;
            chosenSection.node = node;
        }
    }


    /**
     *
     * @param latestStart the latest start time of the job
     * @param curSection current chosen section
     * @param node node
     */
    public void getMinWindow(int estimated, LocalDateTime latestStart, Section curSection, int node, double avgWatts) {

        // how many sections does the job need
        int K = (int) Math.ceil((double) estimated / 1800);
        // remaining time that less than half an hour
        int segment = estimated % 1800;

        int left = 0;
        int right = K - 1;

        // one special case
        if (carbonForecasts.get(right).intensity.forecast < carbonForecasts.get(0).intensity.forecast
            && carbonForecasts.get(0).restTime > segment) {
            double intensity = calIntensity(segment / 3600.0, node, carbonForecasts.get(0).intensity.forecast, avgWatts);
            for (int i = left + 1; i <= right; ++i) {
                intensity += calIntensity(0.5, node, carbonForecasts.get(i).intensity.forecast, avgWatts);
            }
            if (intensity <= curSection.intensity) {
                curSection.startTime = carbonForecasts.get(left).to.minusSeconds(segment);
                curSection.endTime = carbonForecasts.get(right).to;
            }
            left++;   // start from the second window
            right++;
        }
        // get minimal [left, right]
        while (right < 48) {
            // if the start time of this window is already after the latest start time, return
            if (carbonForecasts.get(left).from.isAfter(latestStart)) {
                break;
            }
            setSection(left, right, segment, estimated, curSection, node, avgWatts);
            left++;
            right++;
        }
    }

    /**
     * A small speedup.
     * No matter when the job starts in these k windows, those windows in the middle are absolutely used expect the first
     * window and the last window。 Therefore, just to decide which one need to be absolutely used between the first and
     * the last window, so we can get the minimal carbon intensity.
     * @param left the first index of first window
     * @param right  the last index of last window
     */
    public void setSection(int left, int right, int segment, int estimated,
                           Section curSection, int node, double avgWatts) {
        double intensity;

        // first <= last
        if (carbonForecasts.get(left).intensity.forecast <= carbonForecasts.get(right).intensity.forecast) {
            intensity = getIntensity(estimated, node, left, avgWatts, false);
            if (intensity <= curSection.intensity) {
                curSection.startTime = carbonForecasts.get(left).from;
                curSection.endTime = carbonForecasts.get(right).from.plusSeconds(segment);
                curSection.intensity = intensity;
            }
        } else {
            // first > last
            intensity = getIntensity(estimated, node, right, avgWatts, true);
            if (intensity <= curSection.intensity) {
                curSection.startTime = carbonForecasts.get(left).to.minusSeconds(segment);
                curSection.endTime = carbonForecasts.get(right).to;
                curSection.intensity = intensity;
            }
        }
    }


    /**
     *  i = node / 2 - 1
     *  calculate the carbon emissions produced if the job start executing now
     * @param restTime the time that need to run job
     * @param node node
     * @param index the index of start window
     */
    public double getIntensity(int restTime, int node, int index, double avgWatts, boolean reversed) {
        double intensity = 0;
        while (restTime != 0) {
            int availableTime = carbonForecasts.get(index).restTime;
            if (restTime >= availableTime) {
                intensity += calIntensity(availableTime / 3600.0, node, carbonForecasts.get(index).intensity.forecast, avgWatts);
                restTime -= availableTime;
            } else {
                intensity += calIntensity(restTime / 36000.0, node, carbonForecasts.get(index).intensity.forecast, avgWatts);
                restTime = 0;
            }
            index = reversed ? index - 1: index + 1;
        }
        return intensity;
    }

    /**
     * calculate how much carbon produced at time duration
     * half-hour is a time unit
     */
    public static double calIntensity(double time, int node, int intensity, double avgWatts) {
        double computeWatt = avgWatts * time;
        return computeWatt * intensity * node;
    }

    static class Section {
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public double intensity = Float.MAX_VALUE;
        public int node = 0;

        public Section() {}

        @Override
        public String toString() {
            return "The best configure is scale-out:" + node +
                    ", start time:" + startTime +
                    ", end time:" + endTime +
                    ", produce " + String.format("%.2f", intensity) + "g carbon";
        }
    }

}

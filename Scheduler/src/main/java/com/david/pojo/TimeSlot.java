package main.java.com.david.pojo;


import java.time.LocalDateTime;
import java.util.Deque;

/*

Describe the info of A half hour window carbon intensity
 */
public class TimeSlot {
    public LocalDateTime from;
    public LocalDateTime to;
    public int restTime = 1800;
    public Intensity intensity;
    public Deque<Job> jobList;   // the jobs that need to execute in this slot

    @Override
    public String toString() {

        return "From:" + from.toString() +
                " To:" + to.toString() +
                " Rest time:" + restTime +
                " " +
                intensity.toString();
    }
}

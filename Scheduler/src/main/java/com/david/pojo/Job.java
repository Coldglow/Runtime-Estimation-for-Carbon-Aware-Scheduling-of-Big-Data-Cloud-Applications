package main.java.com.david.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Job {
    private int id;
    private String name;
    private int estimatedTime;
    private int scaleout;
    private LocalDateTime deadline;
    private int maxRunTime;
    private LocalDateTime estimatedStartTime;
    private LocalDateTime estimatedEndTime;

    public Job(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}

package main.java.com.david.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class KMeans extends Job{

    private int observations;
    private int features;
    private int k;

    public KMeans(LocalDateTime deadline, String... args) {
        super(deadline);
        this.observations = Integer.parseInt(args[0]);
        this.features = Integer.parseInt(args[1]);
        this.k = Integer.parseInt(args[2]);
    }
}

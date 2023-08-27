package main.java.com.david.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class Sorts extends Job{
    private int dataSize;
    public Sorts(LocalDateTime deadline, String... args) {
        super(deadline);
        this.dataSize = Integer.parseInt(args[0]);
    }
}

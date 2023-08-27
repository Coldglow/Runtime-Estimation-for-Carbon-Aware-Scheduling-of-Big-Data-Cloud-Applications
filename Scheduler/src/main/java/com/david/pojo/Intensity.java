package main.java.com.david.pojo;

public class Intensity {
    public int forecast;
    public String actual;
    public String index;

    @Override
    public String toString() {
        return "Forecast:" + forecast + " Actual:" + actual + " Index:" + index;
    }
}

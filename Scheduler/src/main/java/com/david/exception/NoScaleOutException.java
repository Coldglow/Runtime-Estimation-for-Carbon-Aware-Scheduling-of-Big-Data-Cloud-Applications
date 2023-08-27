package main.java.com.david.exception;

public class NoScaleOutException extends RuntimeException {
    private int code;
    private String msg;

    public NoScaleOutException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

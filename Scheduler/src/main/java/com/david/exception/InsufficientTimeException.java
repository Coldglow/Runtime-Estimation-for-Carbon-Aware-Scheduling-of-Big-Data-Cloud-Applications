package main.java.com.david.exception;

public class InsufficientTimeException extends RuntimeException{
    private int code;
    private String msg;
    public InsufficientTimeException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

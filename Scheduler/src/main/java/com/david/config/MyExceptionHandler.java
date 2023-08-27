package main.java.com.david.config;

import lombok.extern.slf4j.Slf4j;
import main.java.com.david.exception.NoScaleOutException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * useless, because SpringBoot environment don't start up when running the program
 */
@Slf4j
@ControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(value = NoScaleOutException.class)
    public void exceptionHandler(NoScaleOutException e) {
        log.error(e.getMessage());
        e.printStackTrace();
    }
}

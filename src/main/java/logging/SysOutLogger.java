package logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


public class SysOutLogger implements Logger {
    public enum LogLevel {
        info, debug, warn, error
    }

    private final String className;
    private LogLevel loglevel = LogLevel.info;
    private boolean isDebug = false;
    private boolean isInfo = true;
    private boolean isWarn = true;

    /**
     * initializes the underlying logger object. For the given class.
     *
     * @param: name of class that is going to be logged
     */
    public SysOutLogger(String className) {
        this.className = className;
    }

    @Override
    public void debug(String message) {
        if (isDebug)
            log(getDateTime(), "DEBUG: ", message);
    }

    public void log(String time, String mode, String message) {
        System.out.println(String.format("%s%s %s: %s", time, mode, this.className, message));
    }

    @Override
    public void info(String message) {
        if (isInfo)
            log(getDateTime(), "INFO: ", message);

    }

    @Override
    public void warn(String message) {
        if (isWarn)
            log(getDateTime(), "WARN: ", message);

    }

    @Override
    public void error(String message) {
        log(getDateTime(), "ERROR: ", message);

    }

    @Override
    public void error(String message, Throwable t) {
        log(getDateTime(), "ERROR", message);
        if (t == null || t.getStackTrace() == null) {
            return;
        } else if (t.getStackTrace().length > 10) {
            t.setStackTrace(Arrays.copyOfRange(t.getStackTrace(), 0, 10));
        }

        System.out.println(t.getMessage());
    }

    @Override
    public void error(String message, StackTraceElement[] stackTraceElement) {
        log(getDateTime(), "ERROR", message);
        if (stackTraceElement == null) {
            return;
        } else if (stackTraceElement.length > 10) {
            stackTraceElement = Arrays.copyOfRange(stackTraceElement, 0, 10);
        }
        System.out.println(stackTraceElement);

    }

    public void setLogLevel(LogLevel loglevel) {
        this.loglevel = loglevel;
        isDebug = false;
        isInfo = false;
        isWarn = false;
        switch (loglevel) {
            case debug:
                isDebug = true;
            case info:
                isInfo = true;
            case warn:
                isWarn = true;
            default:
                break;
        }
    }


    public LogLevel getLogLevel() {
        return this.loglevel;
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
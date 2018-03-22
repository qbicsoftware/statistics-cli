package logging;


/**
 * Interface to have implementation and library independent way of logging. Depending on our needs
 * we might want to use log4j, java.util.logging, com.liferay.portal.kernel.log.LogFactoryUtil or
 * some other logging mechanism.
 */

public interface Logger {


    /**
     * Logs debug messages (used for developing/testing only)
     *
     * @param message string to log
     */
    void debug(String message);

    /**
     * Logs info messages (used for important non-error messages, for real problems use warn/error
     * methods)
     *
     * @see #warn(String)
     * @see #error(String)
     * @param message string to log
     */
    void info(String message);

    /**
     * Logs non-critical error messages
     *
     * @param message string to log
     */
    void warn(String message);

    /**
     * Logs error messages (severe faults). If you need more output see
     *
     * @see #error(String, Throwable)
     * @param message string to log
     */
    void error(String message);

    /**
     * Logs error message (severe fault) and adds the top 10 lines of the stack trace (to not overload
     * the console)
     *
     * @param message string to log
     * @param t throwable to expand log
     */
    void error(String message, Throwable t);

    /**
     * Logs error message (severe fault) and adds the top 10 lines of the stack trace (to not overload
     * the console)
     *
     * @param message string to log
     * @param stackTraceElement StackTraceElement to expand log
     */
    void error(String message, StackTraceElement[] stackTraceElement);

}
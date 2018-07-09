package life.qbic.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for the Statistics CLI application.
 * 
 * The purpose of this class is to act as a bridge between the command line and the <i>real</i> implementation of a tool by using a {@link ToolExecutor}.
 */
public class MainEntryPoint {

    private static final Logger LOG = LogManager.getLogger(MainEntryPoint.class);

    /**
     * Main method.
     * 
     * @param args the command-line arguments.
     */
    public static void main(final String[] args) {
        System.out.println("sth here");
        LOG.debug("Starting Main tool");
        final ToolExecutor executor = new ToolExecutor();
        executor.invoke(MainTool.class, MainCommand.class, args);

    }
}
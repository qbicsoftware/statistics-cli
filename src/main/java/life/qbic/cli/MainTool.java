package life.qbic.cli;

import life.qbic.controller.MainController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

/**
 * Implementation of Statistics CLI. Its command-line arguments are contained in instances of {@link MainCommand}.
 */
public class MainTool extends QBiCTool<MainCommand> {

    private static final Logger LOG = LogManager.getLogger(MainTool.class);

    /**
     * Constructor.
     * 
     * @param command an object that represents the parsed command-line arguments.
     */
    public MainTool(final MainCommand command) {
        super(command);
    }

    @Override
    public void execute() {
        // get the parsed command-line arguments
        final MainCommand command = super.getCommand();

        // TODO: do something useful with the obtained command.

        try {
            //@/etc/qbic-stat.config jetty:run
            MainController mainController = new MainController(command);
        } catch (CommandLine.ParameterException e) {
            LOG.error("File could not be parsed. Ensure your config file has the proper fields and delimiter for proper parsing." + e.getMessage());
        }
        
    }

    // TODO: override the shutdown() method if you are implementing a daemon and want to take advantage of a shutdown hook for clean-up tasks
}
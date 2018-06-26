package io.commandline;

import logging.Log4j2Logger;
import logging.Logger;
import picocli.CommandLine;

public class CommandLineParser {

    private static final Logger logger = new Log4j2Logger(CommandLineParser.class);

    /**
     * parses all required parameters and save them into a CommandlineOptions object
     *
     * @param args always has to be a String[] or portlet compatibility breaks!
     */
    public static CommandLineArguments parseCommandlineParameters(String[] args) {
        //no input -> display help: expect one input file
        if (args.length == 0) {
            CommandLine.usage(new CommandLineArguments(), System.out);
            logger.error("Not enough command line arguments were provided.");

            System.exit(0);
        }

        CommandLineArguments commandlineOptions = CommandLine.populateCommand(new CommandLineArguments(), args);

        if (commandlineOptions.isHelp()) {
            CommandLine.usage(new CommandLineArguments(), System.out);
            System.exit(0);
        }

        return commandlineOptions;
    }
}

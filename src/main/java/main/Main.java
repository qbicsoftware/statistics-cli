package main;

import controller.MainController;
import io.commandline.CommandLineParser;
import io.commandline.CommandLineArguments;
import logging.Log4j2Logger;
import logging.Logger;
import picocli.CommandLine;


/**
 * @author fhanssen
 */
public class Main {

    private static Logger logger;

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
        logger = new Log4j2Logger(Main.class);
        //TODO at some point have main only start the program

        try {
            CommandLineArguments commandlineOptions = CommandLineParser.parseCommandlineParameters(args);
            MainController mainController = new MainController(commandlineOptions);
        } catch (CommandLine.ParameterException e) {
            logger.error("File could not be parsed. Ensure your config file has the proper fields and delimiter for proper parsing." + e.getMessage());
        }

    }

}
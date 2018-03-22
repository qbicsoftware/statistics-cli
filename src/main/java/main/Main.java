package main;

import controller.MainController;
import io.input.InputFileParser;
import logging.Log4j2Logger;
import logging.Logger;
import org.apache.commons.cli.*;

import java.io.IOException;

/**
 * @author fhanssen
 */
public class Main {

    private static Logger logger;

    public static void main(String[] args) {

        System.setProperty("log4j.configurationFile","/Users/qbic/Documents/QBiC/statistics-data-retrieval-openbis/src/main/resources/log4j2.xml");
        logger = new Log4j2Logger(Main.class);
        //TODO at some point have main only start the program

        Options options = new Options();

        Option propertyFile = new Option("i", "input", true, "Property file path");
        propertyFile.setRequired(true);
        options.addOption(propertyFile);

        CommandLineParser parser = new BasicParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.error("Could not parse cmd input: " + e.getMessage());
            logger.info("statisitics-data-retrieval-webservice" + options);
            formatter.printHelp("statisitics-data-retrieval-webservice", options);

            System.exit(1);
            return;
        }

        try {
            MainController mainController = new MainController(new InputFileParser(cmd.getOptionValue("input")));
        }catch(IOException e){
            logger.error("File could not be parsed. Ensure your config file has the proper fields and delimiter for proper parsing." + e.getMessage());
        }

    }


}
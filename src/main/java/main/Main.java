package main;

import controller.MainController;
import io.input.InputFileParser;
import org.apache.commons.cli.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

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
            System.out.println(e.getMessage());
            formatter.printHelp("statistics-data-retrieval-webservice", options);

            System.exit(1);
            return;
        }

        try {
            MainController mainController = new MainController(new InputFileParser(cmd.getOptionValue("input")));
            mainController.query();
            mainController.writeToFile();
            mainController.logout();
        } catch (IOException e) {
            System.out.println("File could not be parsed");
            e.printStackTrace();
        }

    }


}
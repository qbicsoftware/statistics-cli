package io.input;

import logging.Log4j2Logger;
import logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author fhanssen
 * Class parses the input credentials stored in a config file.
 */
public class InputFileParser {

    private static final Logger logger = new Log4j2Logger(InputFileParser.class);


    private final BufferedReader bufferedReader;

    private String openBisUrl;
    private String openBisUserName;
    private String openBisPassword;
    private String outputFilename;

    public InputFileParser(String inputFileName) throws IOException{
        bufferedReader = new BufferedReader(new FileReader(inputFileName));
        logger.info("Parse input file: " + inputFileName);
        parse();
    }

    private void parse() throws IOException{
        String line;

        while ( (line = bufferedReader.readLine()) != null)
        {
            String value = line.split("=")[1].trim();
            if(line.startsWith("openBisUrl")){
                openBisUrl = value;
            }
            if(line.startsWith("openBisUserName")){
                openBisUserName = value;
                logger.info("User " + openBisUserName + " is logged in.");
            }
            if(line.startsWith("openBisPassword")){
                openBisPassword = value;
            }
            if(line.startsWith("outputFileName")){
                outputFilename = value;
            }
        }
    }

    public String getOpenBisUrl() {
        return openBisUrl;
    }

    public String getOpenBisUserName() {
        return openBisUserName;
    }

    public String getOpenBisPassword() {
        return openBisPassword;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

}

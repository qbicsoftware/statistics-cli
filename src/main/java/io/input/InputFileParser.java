package io.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class InputFileParser {

    private final BufferedReader bufferedReader;

    private String openBisUrl;
    private String openBisUserName;
    private String openBisPassword;
    private String outputFilename;

    public InputFileParser(String inputFileName) throws IOException{
        bufferedReader = new BufferedReader(new FileReader(inputFileName));
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

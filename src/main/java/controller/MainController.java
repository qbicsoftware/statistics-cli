package controller;

import io.input.InputFileParser;
import io.webservice.OpenBisAccess;
import io.queries.AQuery;
import io.queries.OrganismCountQuery;
import io.writer.YAMLWriter;
import model.data.ChartConfig;
import model.data.MainConfig;

import java.util.Map;

public class MainController {

    private final OpenBisAccess openBisAccess;
    private final String outputFilename;
    private final MainConfig charts;

    //Query classes
    private final AQuery organismCountQuery;


    public MainController(InputFileParser inputFileParser){
        this.openBisAccess = new OpenBisAccess(inputFileParser.getOpenBisUrl(),
                                                inputFileParser.getOpenBisUserName(),
                                                inputFileParser.getOpenBisPassword());
        this.outputFilename = inputFileParser.getOutputFilename();
        this.charts = new MainConfig();

        //init query classes
        organismCountQuery = new OrganismCountQuery(this.openBisAccess.getV3(), this.openBisAccess.getSessionToken());
    }

    public void query(){
        Map<String, ChartConfig> organismCounts = organismCountQuery.query();
        organismCounts.keySet().forEach(name -> charts.addCharts(name, organismCounts.get(name)));
    }

    public void writeToFile(){
        YAMLWriter.writeToFile(outputFilename, charts);
    }

    public void logout(){
        openBisAccess.logout();
    }
}

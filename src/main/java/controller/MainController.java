package controller;

import io.input.InputFileParser;
import io.webservice.OpenBisAccess;
import io.queries.AQuery;
import io.queries.OrganismCountQuery;
import io.writer.YAMLWriter;
import model.data.ChartConfig;
import model.data.MainConfig;

import java.util.Map;

/**
 * @author fhanssen
 * MainController handles all major oprations. It is called from the main class, establishes a connection to OpenBis then
 * executes all queries and adds them to the config file. Lastly the connection has to be closed.
 */

public class MainController {

    private final OpenBisAccess openBisAccess;
    private final String outputFilename;
    private final MainConfig charts;

    //Query classes
    private final AQuery organismCountQuery;

    public MainController(InputFileParser inputFileParser) {

        this.openBisAccess = new OpenBisAccess(inputFileParser.getOpenBisUrl(),
                inputFileParser.getOpenBisUserName(),
                inputFileParser.getOpenBisPassword());
        this.outputFilename = inputFileParser.getOutputFilename();
        this.charts = new MainConfig();

        //init query classes
        organismCountQuery = new OrganismCountQuery(this.openBisAccess.getV3(), this.openBisAccess.getSessionToken());


            query();


        writeToFile();
        logout();
    }

    /**
     * This method executes all queries. If new queries should be run, they need to be started from here. The resulting
     * ChartConfigs need to be added to the MainConfig charts.
     */
    private void query() {

        try {
            Map<String, ChartConfig> organismCounts = organismCountQuery.query();
            organismCounts.keySet().forEach(name -> charts.addCharts(name, organismCounts.get(name)));
        }catch(Exception e){
            e.printStackTrace();
        }


        //TODO 3: Add your query call here
        //TODO 4: Add your resulting chart config(s) to 'charts'
    }

    private void writeToFile() {
        YAMLWriter.writeToFile(outputFilename, charts);
    }

    private void logout() {
        openBisAccess.logout();
    }
}

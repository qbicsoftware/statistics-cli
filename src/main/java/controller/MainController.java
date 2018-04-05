package controller;

import io.input.InputFileParser;
import io.queries.AvailablePipelinesQuery;
import io.webservice.OpenBisAccess;
import io.queries.AQuery;
import io.queries.OrganismCountQuery;
import io.writer.YAMLWriter;
import logging.Log4j2Logger;
import logging.Logger;
import model.data.ChartConfig;
import model.data.MainConfig;

import java.util.Map;

/**
 * @author fhanssen
 * MainController handles all major oprations. It is called from the main class, establishes a connection to OpenBis then
 * executes all queries and adds them to the config file. Lastly the connection has to be closed.
 */

public class MainController {

    private static Logger logger = new Log4j2Logger(MainController.class);


    private final OpenBisAccess openBisAccess;
    private final String outputFilename;
    private final MainConfig charts;

    //Query classes
    private final AQuery organismCountQuery;
    private final AQuery availablePipelinesQuery;

    public MainController(InputFileParser inputFileParser) {

        logger.info("Establish access to OpenBis");

        this.openBisAccess = new OpenBisAccess(inputFileParser.getOpenBisUrl(),
                inputFileParser.getOpenBisUserName(),
                inputFileParser.getOpenBisPassword());

        this.outputFilename = inputFileParser.getOutputFilename();

        this.charts = new MainConfig();

        //init query classes
        organismCountQuery = new OrganismCountQuery(this.openBisAccess.getV3(), this.openBisAccess.getSessionToken());
        availablePipelinesQuery = new AvailablePipelinesQuery();

        logger.info("Start queries");
        query();

        logger.info("Write results to " + outputFilename);
        writeToFile();

        logger.info("Log out of OpenBis");
        logout();
    }

    /**
     * This method executes all queries. If new queries should be run, they need to be started from here. The resulting
     * ChartConfigs need to be added to the MainConfig charts.
     */
    private void query() {

        try {
            logger.info("Run OrganismCounts query");
            Map<String, ChartConfig> organismCounts = organismCountQuery.query();
            organismCounts.keySet().forEach(name -> charts.addCharts(name, organismCounts.get(name)));

            logger.info("Run AvailablePipelines query");
            Map<String, ChartConfig> github = availablePipelinesQuery.query();
            github.keySet().forEach(name -> charts.addCharts(name, github.get(name)));

        }catch(Exception e){
            e.printStackTrace();
            logger.info("Queries failed with: " + e.getMessage());
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

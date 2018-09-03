package life.qbic.controller;

import life.qbic.cli.MainCommand;
import life.qbic.io.queries.*;
import life.qbic.io.webservice.OpenBisAccess;
import life.qbic.io.writer.YAMLWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import submodule.data.ChartConfig;
import submodule.data.MainConfig;

import java.util.Map;

/**
 * @author fhanssen
 * MainController handles all major oprations. It is called from the main class, establishes a connection to OpenBis then
 * executes all queries and adds them to the config file. Lastly the connection has to be closed.
 */

public class MainController {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    private final OpenBisAccess openBisAccess;
    private final String outputFilename;
    private final MainConfig charts;

    //Query classes
    private final AQuery organismCountQuery;
    private final AQuery availablePipelinesQuery;
    private final AQuery projectsTechnologiesQuery;
    private final AQuery sampleTypeQuery;

    public MainController(MainCommand command) {
        logger.info("Establish access to OpenBis");

        this.openBisAccess = new OpenBisAccess(command.openBISUrl,
                command.openBISUsername,
                command.openBISPassword);

        this.outputFilename = command.outputFileName;

        this.charts = new MainConfig();

        //init query classes
        organismCountQuery = new OrganismCountQuery(this.openBisAccess.getV3(), this.openBisAccess.getSessionToken(),
                command.ncbiTaxUrl, command.domainThreshold);
        availablePipelinesQuery = new WorkflowQueries(this.openBisAccess.getV3(), this.openBisAccess.getSessionToken(),
                command.gitHubUrl, command.gitHubHeaderKey, command.gitHubHeaderValue, command.maxNumRepos);
        projectsTechnologiesQuery = new ProjectsTechnologiesQuery(this.openBisAccess.getV3(), this.openBisAccess.getSessionToken());
        sampleTypeQuery = new SampleTypeQuery(this.openBisAccess.getV3(), this.openBisAccess.getSessionToken());

        logger.info("Start queries");
        queryAll();

        logger.info("Write results to " + outputFilename);
        writeToFile();

        logger.info("Log out of OpenBis");
        logout();
    }

    /**
     * This method executes all queries. If new queries should be run, they need to be started from here. The resulting
     * ChartConfigs need to be added to the MainConfig charts.
     */
    private void queryAll() {

       query(organismCountQuery);
//       query(availablePipelinesQuery);
//       query(projectsTechnologiesQuery);
//       query(sampleTypeQuery);

        //TODO 3: Add your query call here

    }

    private void query(AQuery queryClass){
        try {
            logger.info("Run " + queryClass.getClass() +" query");
            Map<String, ChartConfig> result = queryClass.query();
            result.keySet().forEach(name -> charts.addCharts(name, result.get(name)));
        } catch (Exception e) {
            logger.error("Query " + queryClass.getClass() +"  failed with: " + e.getMessage());
            logger.error(e.getStackTrace());

        }
    }


    private void writeToFile() {
        YAMLWriter.writeToFile(outputFilename, charts);
    }

    private void logout() {
        openBisAccess.logout();
    }
}

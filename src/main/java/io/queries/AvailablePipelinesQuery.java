package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import io.queries.utils.Helpers;
import io.webservice.REST;
import javafx.scene.chart.Chart;
import logging.Log4j2Logger;
import logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import submodule.data.ChartConfig;
import submodule.data.ChartSettings;
import submodule.lexica.ChartNames;

/**
 * @author fhanssen
 * This Query class, accesses GitHub of qbicsoftware and retrieves all repos that have the topic 'workflow'.
 * Their information is retrieved and only the decription(yCategories) and stargazer count(date) is stored in the config
 */

public class AvailablePipelinesQuery implements IQuery {

    //TODO Waiting for Feedback from PMs

    private static final Logger logger = new Log4j2Logger(AvailablePipelinesQuery.class);

    //TODO: Maybe move these to config, in case they change over time
    private final String GITHUB_Url = "https://api.github.com/orgs/qbicsoftware/repos";
    private final String HEADER_KEY = "Accept";
    private final String HEADER_VALUE = "application/vnd.github.mercy-preview+json";

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private final Map<String, List<LinkedTreeMap>> workflows = new HashMap<>();

    private final Map<String, ChartConfig> result = new HashMap<>();

    private final Map<String, Object> workflowTypeCountResult = new HashMap<>();

    public AvailablePipelinesQuery(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }

    public Map<String, ChartConfig> query() {

        logger.info("Run workflow query");

        logger.info("Clear results");
        clear();

        computeWorkflowExecutionCounts();

        logger.info("Get available QBiC workflows from GitHub via API");
        //Exceeded query num
        workflowTypeCountResult.keySet().forEach(type -> getAvailableWorkflows(type.toLowerCase()));

        logger.info("Set results");
        workflows.keySet().forEach(type ->
            result.put(type, customizeChartConfig(workflows.get(type))));
        result.put("Workflow counts", Helpers.generateChartConfig(workflowTypeCountResult, "wf counts", "wf counts"));
        return result;
    }

    private void clear() {
        workflows.clear();
        result.clear();
        workflowTypeCountResult.clear();
    }

    //call this for each workflow and name it after its type
    private ChartConfig customizeChartConfig(List<LinkedTreeMap> list) {
        Map<String, Object> map = new HashMap<>();
        list.forEach(a -> map.put(Objects.toString(a.get("url"), ""), a.get("stargazers_count")));

        ChartConfig chartConfig = Helpers.generateChartConfig(map, "stargazers_count","Available Workflows");

        List<String> descriptions = new ArrayList<>();
        list.forEach(a -> descriptions.add(Objects.toString(a.get("description"), "")));
        chartConfig.getSettings().setyCategories(new ArrayList<>(descriptions));

        return chartConfig;
    }

    private void getAvailableWorkflows(String type) {
        int page_counter = 1; //GitHub API does not support pagination right now, 100 results can be displayed at once
        // at most, so we have to access all pages by hand

        String line = "";
        while (!"[]".equals(line)) { // no results are shown with [] (empty page sign)from API
            try (BufferedReader rd = new BufferedReader(
                    new InputStreamReader(REST.call(GITHUB_Url.concat("?page=".concat(String.valueOf(page_counter))
                                    .concat("&per_page=100")),
                            HEADER_KEY,
                            HEADER_VALUE)))) {

                while ((line = rd.readLine()) != null && !"[]".equals(line)) {
                    retrieveIfWorkflow(line, type);
                }

            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Access of GitHub via API failed with: " + e.getMessage());
            }
            //Access next 100 results
            page_counter++;

            if(page_counter > 5){
                System.out.println("GIT CALL ERROR");
                break;
            }
        }
    }

    private void retrieveIfWorkflow(String line, String type) {
        GsonBuilder builder = new GsonBuilder();
        Object o = builder.create().fromJson(line, Object.class);
        ((ArrayList) o).forEach(id -> {
            if (((LinkedTreeMap) id).containsKey("topics")) {
                if (((ArrayList) ((LinkedTreeMap) id).get("topics")).contains("workflow") && ((ArrayList) ((LinkedTreeMap) id).get("topics")).contains(type)) {
                    Helpers.addEntryToStringListMap(workflows,type, (LinkedTreeMap)id);
                }
            }

        });
    }

    private void computeWorkflowExecutionCounts() {
        //Count how many times eac workflow was executed

        ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
        experimentSearchCriteria.withProperty("Q_WF_NAME");

        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withProperties();
        experimentFetchOptions.withTags();
        experimentFetchOptions.withType();

        SearchResult<Experiment> experimentSearchResult = v3.searchExperiments(sessionToken, experimentSearchCriteria, experimentFetchOptions);
        experimentSearchResult.getObjects().forEach(experiment -> {
            //Don't do this for the moment because we don't have a proper mapping right now
//            int counter = 1;
//
//            if (workflowCountResult.containsKey(e.getProperty("Q_WF_NAME"))) {
//                counter += workflowCountResult.get(e.getProperty("Q_WF_NAME"));
//            }
//            workflowCountResult.put(e.getProperty("Q_WF_NAME"), counter);

            String[] arr = experiment.getType().toString().split("_");
            if (arr[1].equals("WF")) {
                Helpers.addEntryToStringCountMap(workflowTypeCountResult, arr[2], 1);
            }
        });
    }

}

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
import logging.Log4j2Logger;
import logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import submodule.data.ChartConfig;
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

    private final List<LinkedTreeMap> allGithubWorkflows = new ArrayList<>();

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

        logger.info("Count number of times workflows have been executed via OpenBis and summarize it by types.");
        computeWorkflowExecutionCounts();

        logger.info("Get available QBiC workflows from GitHub via API and sort them by type");
        //Sort all workflows by type
        //workflowTypeCountResult.keySet().forEach(type -> getAvailableWorkflows(type.toLowerCase()));
        //Add any workflows with no type
        //getAvailableWorkflows("other");
        getAvailableWorkflows();
        workflowTypeCountResult.keySet().forEach(type -> sortAvailableWorkflowsByType(type));
        sortAvailableWorkflowsByType("other");
        logger.info("Set results");
        workflows.keySet().forEach(type ->
                result.put(ChartNames.Available_Workflows_.toString().concat(type.toUpperCase()), customizeChartConfig(workflows.get(type), type.toUpperCase())));
        if (workflows.containsKey("other")) {
            result.put(ChartNames.Available_Workflows_.toString().concat("other"), customizeChartConfig(workflows.get("other"), "Other"));
        } else {
            result.put(ChartNames.Available_Workflows_.toString().concat("other"), new ChartConfig());
        }

        result.put(ChartNames.Workflow_Execution_Counts.toString(), Helpers.generateChartConfig(workflowTypeCountResult, "Counts", "Workflow execution ratio"));

        return result;
    }

    private void clear() {
        workflows.clear();
        result.clear();
        workflowTypeCountResult.clear();
    }

    //call this for each workflow and name it after its type
    private ChartConfig customizeChartConfig(List<LinkedTreeMap> list, String title) {
        Map<String, Object> resultMap = new HashMap<>();
        list.forEach(a -> resultMap.put(Objects.toString(a.get("url"), ""), a.get("stargazers_count")));

        ChartConfig chartConfig = Helpers.generateChartConfig(resultMap, "stargazers_count", "Available Workflows ".concat(title));

        List<String> descriptions = new ArrayList<>();
        list.forEach(a -> descriptions.add(Objects.toString(a.get("description"), "")));
        chartConfig.getSettings().setyCategories(new ArrayList<>(descriptions));

        return chartConfig;
    }

    private void getAvailableWorkflows() {
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
                    retrieveIfWorkflow(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Access of GitHub via API failed with: " + e.getMessage());
            }
            //Access next 100 results
            page_counter++;

            //TODO find some sort of safety net here
            if (page_counter > 5) {
                System.out.println("GIT CALL ERROR");
                break;
            }
        }
    }

    private void retrieveIfWorkflow(String line) {
        GsonBuilder builder = new GsonBuilder();
        Object o = builder.create().fromJson(line, Object.class);
        ((ArrayList) o).forEach(id -> {
            if (((LinkedTreeMap) id).containsKey("topics")) {
                if (((ArrayList) ((LinkedTreeMap) id).get("topics")).contains("workflow")) {//&& ((ArrayList) ((LinkedTreeMap) id).get("topics")).contains(type)) {
                    //Helpers.addEntryToStringListMap(workflows, type, (LinkedTreeMap)id);
                    allGithubWorkflows.add((LinkedTreeMap) id);
                }
            }

        });
    }

    private void sortAvailableWorkflowsByType(String type) {
        allGithubWorkflows.forEach(id -> {
            if (((ArrayList) id.get("topics")).contains(type)) {
                Helpers.addEntryToStringListMap(workflows, type, (LinkedTreeMap)id);
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

package life.qbic.io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import life.qbic.io.queries.utils.Helpers;
import life.qbic.io.queries.utils.lexica.SpaceBlackList;
import life.qbic.io.queries.utils.lexica.WorkflowSubtypes;
import life.qbic.io.webservice.REST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import submodule.data.ChartConfig;
import submodule.lexica.ChartNames;


/**
 * @author fhanssen
 * This Query class, accesses GitHub of qbicsoftware and retrieves all repos that have the topic 'workflow'.
 * Their information is retrieved and only the decription(yCategories) and stargazer count(date) is stored in the config
 */

public class WorkflowQueries extends AQuery {

    private static final Logger logger = LogManager.getLogger(WorkflowQueries.class);

    //TODO: Maybe move these to config, in case they change over time
    private static final String GITHUB_Url = "https://api.github.com/orgs/qbicsoftware/repos";
    private static final String HEADER_KEY = "Accept";
    private static final String HEADER_VALUE = "application/vnd.github.mercy-preview+json";

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private final List<LinkedTreeMap> allGithubWorkflows = new ArrayList<>();

    private final Map<String, List<LinkedTreeMap>> workflows = new HashMap<>();

    private final Map<String, ChartConfig> result = new HashMap<>();

    private final Map<String, Object> workflowTypeCountResult = new HashMap<>();

    private SearchResult<Sample> searchResult;

    public WorkflowQueries(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }

    public Map<String, ChartConfig> query() {

        logger.info("Run workflow query");

        logger.info("Clear results");
        clear();

        logger.info("Count number of times workflows have been executed via OpenBis and summarize it by types.");
        retrieveSamplesFromOpenBis();
        //removeBlacklistedSpaces();
        //TODO comment this back in
        // TODO however for testing I somehow only have access to chickenfarm stuff anymore, so it has to be commented out

        countWorkflowExecutionCounts();

        logger.info("Get available QBiC workflows from GitHub via API");
        getAvailableWorkflows();

        logger.info("Sort workflows by type");
        workflowTypeCountResult.keySet().forEach(type -> {
            sortAvailableWorkflowsByType(type.toLowerCase());
        });
        //Add this LAST!
        sortAvailableWorkflowsByType("other");


        logger.info("Set results");
        workflows.keySet().forEach(type ->
                result.put(ChartNames.Available_Workflows_.toString().concat(type.toUpperCase()), customizeChartConfig(workflows.get(type), type.toUpperCase())));

        result.put(ChartNames.Workflow_Execution_Counts.toString(), Helpers.addPercentages(Helpers.generateChartConfig(workflowTypeCountResult, "Counts", "Workflow Execution Counts", "Workflow")));

        return result;
    }

    private void clear() {
        workflows.clear();
        result.clear();
        workflowTypeCountResult.clear();
    }

    @Override
    void removeBlacklistedSpaces() {

        List<Sample> removables = new ArrayList<>();
        searchResult.getObjects().forEach(experiment -> {
            if (SpaceBlackList.getList().contains(experiment.getSpace().getCode())) {
                removables.add(experiment);
            }
        });
        searchResult.getObjects().removeAll(removables);

        logger.info("Removed results from blacklisted OpenBis Spaces: " + SpaceBlackList.getList().toString());

    }

    //call this for each workflow and name it after its type
    private ChartConfig customizeChartConfig(List<LinkedTreeMap> list, String title) {
        Map<String, Object> resultMap = new HashMap<>();
        list.forEach(a -> resultMap.put(Objects.toString(a.get("url"), ""), a.get("stargazers_count")));

        ChartConfig chartConfig = Helpers.generateChartConfig(resultMap, "stargazers_count", "Available Workflows ".concat(title), "Workflow");

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
                logger.error("GIT CALL ERROR");
                break;
            }
        }
    }

    private void retrieveIfWorkflow(String line) {
        GsonBuilder builder = new GsonBuilder();
        Object o = builder.create().fromJson(line, Object.class);
        ((ArrayList) o).forEach(id -> {
            if (((LinkedTreeMap) id).containsKey("topics")) {
                if (((ArrayList) ((LinkedTreeMap) id).get("topics")).contains("workflow")) {
                    allGithubWorkflows.add((LinkedTreeMap) id);
                }
            }

        });
    }

    private void sortAvailableWorkflowsByType(String type) {
        List<LinkedTreeMap> removables = new ArrayList<>();
        allGithubWorkflows.forEach(id -> {
            if (((ArrayList) id.get("topics")).contains(type) || type.equals("other")) {

                List<String> listOne = Helpers.getCommonElements((ArrayList<String>) id.get("topics"),
                         WorkflowSubtypes.getList());

                if(listOne.size() == 0){
                    Helpers.addEntryToStringListMap(workflows, type, (LinkedTreeMap)id);
                }
                listOne.forEach(l -> {
                    Helpers.addEntryToStringListMap(workflows, type.concat("_").concat(l), (LinkedTreeMap)id);
                });
                removables.add(id);
            }
        });
        allGithubWorkflows.removeAll(removables);
    }

    private void retrieveSamplesFromOpenBis() {
        //Retrieve samples
        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withType();
        sampleFetchOptions.withSpace();

        searchResult = v3.searchSamples(sessionToken, sampleSearchCriteria, sampleFetchOptions);
    }

    private void countWorkflowExecutionCounts(){
        //Count workflow execution times
        searchResult.getObjects().forEach(o ->{
            String[] arr = o.getType().toString().split("_");
            if(arr.length > 1) {
                if (arr[1].equals("WF")) {
                    System.out.println(arr[2]);
                    Helpers.addEntryToStringCountMap(workflowTypeCountResult, arr[2], 1);
                }
            }
        });
    }

}

package io.queries;

import io.queries.utils.lexica.ChartNames;
import io.webservice.REST;
import logging.Log4j2Logger;
import logging.Logger;
import model.data.ChartConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import model.data.ChartSettings;
import org.json.*;

/**
 * @author fhanssen
 *  This Query class, accesses GitHub of qbicsoftware and retrieves all repos that have the topic 'workflow'.
 *  Their information is retrieved and only the decription(yCategories) and stargazer count(date) is stored in the config
 */

public class AvailablePipelinesQuery implements IQuery{

    private static final Logger logger = new Log4j2Logger(AvailablePipelinesQuery.class);

    //TODO: Maybe move these to config, in case they change over time
    private final String GITHUB_Url = "https://api.github.com/orgs/qbicsoftware/repos";
    private final String HEADER_KEY = "Accept";
    private final String HEADER_VALUE = "application/vnd.github.mercy-preview+json";

    private final JSONArray workflow;

    public AvailablePipelinesQuery() {
        this.workflow = new JSONArray();
    }


    public Map<String, ChartConfig> query() {

        logger.info("Run workflow query");

        logger.info("Clear results");
        clear();

        logger.info("Get available QBiC workflows from GitHub via API");
        getAvailableWorkflows();

        Map<String, ChartConfig> result = new HashMap<>();
        logger.info("Set results");
        result.put(ChartNames.Workflow.toString(), generateChartConfig(ChartNames.Workflow.toString()));

        return result;
    }

    private void clear(){
        //Unfortunately no .clear()/.removeAll() method
        for(int i = 0; i < workflow.length(); i++){
            workflow.remove(i);
        }
    }

    private void getAvailableWorkflows() {
        int page_counter = 1; //GitHub API does not support pagination right now, 100 results can be displayed at once
                              // at most, so we have to access all pages by hand
        String line = "";
        while (!"[]".equals(line)) { // no results are shown with [] form API
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
        }
    }

    private void retrieveIfWorkflow(String line) {
        JSONArray temp = new JSONArray(line);
        temp.forEach(t -> {
            //Check if the repo has topics and if one of the sopics is 'workflow'
            if (((JSONObject) t).has("topics")) {
                JSONArray a = (JSONArray)((JSONObject) t).get("topics");
                if(a.toString().contains("workflow")) {
                    //if so, then add to results
                    workflow.put(t);

                }
            }

        });
    }

    private ChartConfig generateChartConfig(String title) {

        logger.info("Generate ChartConfig for: " + title);

        ChartConfig github = new ChartConfig();

        //Chart settings with title
        ChartSettings githubSettings = new ChartSettings(title);

        //Set xCategories = API URL
        List<String> repos = new ArrayList<>();
        workflow.forEach(a -> repos.add(Objects.toString(((JSONObject)a).get("url"), "")));
        githubSettings.setxCategories(new ArrayList<>(repos));

        //Set yCategories = descriptions
        List<String> descriptions = new ArrayList<>();
        workflow.forEach(a -> descriptions.add(Objects.toString(((JSONObject) a).get("description"), "")));
        githubSettings.setyCategories(new ArrayList<>(descriptions));

        //Add settings to chart config
        github.setSettings(githubSettings);

        //Add chart data: be careful with order of data: must match category order
        Map<Object, ArrayList<Object>> stars = new HashMap<>();
        ArrayList<Object> starcounts = new ArrayList<>();
        workflow.forEach(a -> starcounts.add(((JSONObject)a).get("stargazers_count")));

        stars.put("stargazers_count", starcounts);
        github.setData(stars);

        return github;
    }
}

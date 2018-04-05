package io.queries;

import io.queries.utils.lexica.ChartNames;
import io.webservice.REST;
import model.data.ChartConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import model.data.ChartSettings;
import org.json.*;

/**
 * @author fhanssen
 */

public class AvailablePipelinesQuery extends AQuery {

    private final String GITHUB_Url = "https://api.github.com/orgs/qbicsoftware/repos";
    private final String HEADER_KEY = "Accept";
    private final String HEADER_VALUE = "application/vnd.github.mercy-preview+json";

    public AvailablePipelinesQuery() {
        super();
    }


    @Override
    public Map<String, ChartConfig> query() {

        int page_counter = 1;
        String line = "";
        JSONArray array = new JSONArray();
        while (!"[]".equals(line)) {
            try (BufferedReader rd = new BufferedReader(
                    new InputStreamReader(REST.call(GITHUB_Url.concat("?page=".concat(String.valueOf(page_counter)).concat("&per_page=100")),
                                                    HEADER_KEY,
                                                    HEADER_VALUE)))) {
                while ((line = rd.readLine()) != null && !"[]".equals(line)) {
                    JSONArray temp = new JSONArray(line);
                    temp.forEach(t -> {
                        if (((JSONObject) t).has("topics")) {
                            JSONArray a = (JSONArray)((JSONObject) t).get("topics");
                            if(a.toString().contains("workflow")) {
                                array.put(t);

                            }
                        }

                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            page_counter++;
        }

        Map<String, ChartConfig> result = new HashMap<>();

        result.put(ChartNames.GitHub.toString(), generateChartConfig(array, "Workflows"));

        return result;
    }

    private ChartConfig generateChartConfig(JSONArray array, String title) {


        ChartConfig github = new ChartConfig();

        //Add chart settings
        ChartSettings githubSettings = new ChartSettings(title);
        //Set xCategories
        List<String> repos = new ArrayList<>();
        array.forEach(a -> repos.add(Objects.toString(((JSONObject)a).get("url"), "")));

        List<String> descriptions = new ArrayList<>();
        array.forEach(a -> descriptions.add(Objects.toString(((JSONObject) a).get("description"), "")));


        githubSettings.setxCategories(new ArrayList<>(repos));
        githubSettings.setyCategories(new ArrayList<>(descriptions));

        //Add settings to chart config
        github.setSettings(githubSettings);

        //Add chart data: be careful with order of data: must match xCategory order
        Map<Object, ArrayList<Object>> stars = new HashMap<>();

        //This is necessary to always ensure proper order and mapping of key value pairs in YAML!
        ArrayList<Object> starcounts = new ArrayList<>();
        array.forEach(a -> starcounts.add(((JSONObject)a).get("stargazers_count")));

        stars.put("stars", starcounts);
        github.setData(stars);

        return github;
    }
}

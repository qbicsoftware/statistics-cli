package io.queries.utils;

import logging.Log4j2Logger;
import logging.Logger;
import submodule.data.ChartConfig;
import submodule.data.ChartSettings;

import java.util.*;

/**
 * @author fhanssen
 * This static class holds useful helper methods, that have to be reused for multiple queries.
 */
public final class Helpers {

    private static final Logger logger = new Log4j2Logger(Helpers.class);


    public static void addEntryToStringCountMap(Map map, String key, int count) {
        if (map.containsKey(key)) {
            int counter = (int)map.get(key);
            map.put(key, counter + count);
        } else {
            map.put(key, count);
        }
    }

    public static void addEntryToStringListMap(Map map, String key, Object listElement) {

        if (map.containsKey(key)) {
            List l = (List)map.get(key);
            l.add( listElement);
            map.put(key, l);
        } else {
            List l = new ArrayList();
            l.add(listElement);
            map.put(key, l);
        }
    }

    public static List<String> getCommonElements(List<String> listOne, List<String> listTwo){
        listOne.replaceAll(String::toUpperCase);
        listTwo.replaceAll(String::toUpperCase);
        listOne.retainAll(listTwo);
        return listOne;
    }

    /**
     * Method turns retrieved data to a chart config. can be arbitrarily extended with more parameters. Be careful with adding data.
     * You somehow need to ensure that your data order matches the category order (here xCategories holds the names, data
     * holds the respective values, the need to be in the correct order to allow matching later!)
     *
     * Most common: xcategories and data: so this is used here, anytng else has to be added to the returned chartconfig
     * @param result Map holding categories as key, and the respective values as values
     * @param name
     * @param chartTitle  Chart title, stored in config and later displayed
     * @return ChartConfig
     */
    public static ChartConfig generateChartConfig(Map<String, Object> result, String name, String chartTitle) {

        logger.info("Generate ChartConfig for: " + name + " " + chartTitle);

        ChartConfig chartConfig = new ChartConfig();

        //Add chart settings
        ChartSettings chartSettings = new ChartSettings();
        chartSettings.setTitle(chartTitle);
        //Set xCategories
        List<String> xCategories = new ArrayList<>(result.keySet());

        chartSettings.setxCategories(new ArrayList<>(xCategories));

        //Add settings to chart config
        chartConfig.setSettings(chartSettings);

        //Add chart data: be careful with order of data: must match xCategory order
        Map<Object, ArrayList<Object>> count = new HashMap<>();

        //This is necessary to always ensure proper order and mapping of key value pairs in YAML!
        ArrayList<Object> list = new ArrayList<>();
        xCategories.forEach(o -> list.add(result.get(o)));
        count.put(name, list);
        chartConfig.setData(count);

        return chartConfig;
    }


}

package life.qbic.io.queries.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import submodule.data.ChartConfig;
import submodule.data.ChartSettings;

import java.util.*;

/**
 * @author fhanssen
 * This static class holds useful helper methods, that have to be reused for multiple queries.
 */
public final class Helpers {

    private static final Logger logger = LogManager.getLogger(Helpers.class);


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

    public static void addEntryToSetCountMap(Map map, Set key, int count) {

        if (map.containsKey(key)) {
            int counter = (int)map.get(key);
            map.put(key, counter + count);
        } else {
            map.put(key, count);
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
     * Most common: xcategories and data: so this is used here, anything else has to be added to the returned chartconfig
     * @param result Map holding categories as key, and the respective values as values
     * @param name name in mainconfig, should be unique to all others
     * @param chartTitle  Chart title, stored in config and later displayed
     * @param tabTitle Title displayed in tab header
     * @return ChartConfig
     */
    public static ChartConfig generateChartConfig(Map<String, Object> result, String name, String chartTitle, String tabTitle) {

        logger.info("Generate ChartConfig for: " + name + " " + chartTitle);

        ChartConfig chartConfig = new ChartConfig();

        //Add chart settings
        ChartSettings chartSettings = new ChartSettings();
        chartSettings.setTitle(chartTitle);
        chartSettings.setTabTitle(tabTitle);
        System.out.println("\nTabtitle " + tabTitle);
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


    /**
     * This method adds the percentages as yCategories to a given chartconfig. This means it takes each value from
     * the xCategories and computes its percentage to the total sum. The respective percentage can then be
     * found at the same position in the yCategories.
     * @param chartConfig
     * @return
     */
    public static ChartConfig addPercentages(ChartConfig chartConfig){
        int totalCount = 0;
        Object[] objectArray = chartConfig.getData().keySet().toArray(new Object[0]);
        String[] keySet = Arrays.asList(objectArray).toArray(new String[objectArray.length]);

        //Compute total count
        for (String aKeySet : keySet) {
            for (int i = 0; i < chartConfig.getData().get(aKeySet).size(); i++) {
                totalCount += (int)chartConfig.getData().get(aKeySet).get(i);
            }
        }

        //Compute percentage and round to one decimal position
        List<Double> yCategories = new ArrayList<>();
        for (String aKeySet : keySet) {
            for (int i = 0; i < chartConfig.getData().get(aKeySet).size(); i++) {
                yCategories.add( Math.round(10.0 * (100.0 * (double) ((int)chartConfig.getData().get(aKeySet).get(i))/(double) ((int)totalCount)))/ 10.0);
            }
        }
        chartConfig.getSettings().setyCategories(new ArrayList<>(yCategories));
        return chartConfig;
    }

    public static boolean isOmicsRun(String name) {
        String[] array = name.split("_");
        return array[array.length - 1].equals("RUN") && !array[1].equals("WF");
    }



}

package submodule.data;

import java.util.Map;
import java.util.HashMap;



/**
 * @author fhanssen
 *  * This needs to be synced with the MainConfig class from the data tool.
 * Don't delete any Getters or Setters, even if they are shown as unused. SnakeYAMl requieres them.
 */
public class MainConfig {

    private Map<String, ChartConfig> charts = new HashMap<>();

    public Map<String, ChartConfig> getCharts() {
        return charts;
    }

    public void setCharts(Map<String, ChartConfig> charts) {
        this.charts = charts;
    }

    public void addCharts(String chartName, ChartConfig chartConfig){
        this.charts.put(chartName, chartConfig);
    }

    @Override public String toString() {
        return "YamlConfig{" +
                "charts=" + charts +
                '}';
    }
}

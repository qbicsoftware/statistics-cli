package model.data;

import java.util.HashMap;
import java.util.Map;

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

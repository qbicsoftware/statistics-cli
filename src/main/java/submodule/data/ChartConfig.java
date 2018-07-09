package submodule.data;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author fhanssen
 * This needs to be synced with the ChartConfig class from the data tool.
 * Don't delete any Getters or Setters, even if they are shown as unused. SnakeYAMl requieres them.
 */
public class ChartConfig {

    private ChartSettings settings;
    private Map<Object,ArrayList<Object>> data;

    public ChartSettings getSettings() {
        return settings;
    }

    public void setSettings(ChartSettings settings) {
        this.settings = settings;
    }

    public Map<Object,ArrayList<Object>>  getData() {
        return data;
    }

    public void setData(Map<Object,ArrayList<Object>> data) {
        this.data = data;
    }
}

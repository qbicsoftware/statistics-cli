package submodule.data;

import java.util.ArrayList;

/**
 * @author fhanssen
 *  * This needs to be synced with the ChartSetting class from the data tool.
 * Don't delete any Getters or Setters, even if they are shown as unused. SnakeYAMl requieres them.
 */
public class ChartSettings {

    private ArrayList<Object> xCategories;
    private ArrayList<Object> yCategories;
    private String title;
    private String subtitle;
    private String xAxisTitle;
    private String yAxisTitle;


    public ArrayList<Object> getxCategories() {
        return xCategories;
    }

    public void setxCategories(ArrayList<Object> xCategories) {
        this.xCategories = xCategories;
    }

    public ArrayList<Object> getyCategories() {
        return yCategories;
    }

    public void setyCategories(ArrayList<Object> yCategories) {
        this.yCategories = yCategories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getxAxisTitle() {
        return xAxisTitle;
    }

    public void setxAxisTitle(String xAxisTitle) {
        this.xAxisTitle = xAxisTitle;
    }

    public String getyAxisTitle() {
        return yAxisTitle;
    }

    public void setyAxisTitle(String yAxisTitle) {
        this.yAxisTitle = yAxisTitle;
    }

}

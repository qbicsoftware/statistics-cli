package io.queries.numbersQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Lukas Heumos
 * This is a data container for all factual openBIS numbers.
 * <b>Note:</b> some of those have to be updated <b>manually</b>. Contact the system administrator for an update of those numbers.
 */
public class Numbers {

    /**
     * These are updated <b>automatically!</b>
     */
    private Integer datasets;
    private Integer projects;
    private Integer openProjects;
    private Integer samples;
    private Integer rawDataSize;

    //if we are able to differ between used technologies we should add those additional numbers here

    /**
     * These have to be updated <b>manually!</b>
     * The unit of this number is terabytes!
     */
    private final Integer CURRENTSTORAGESIZE = 0;

    final List<Integer> allNumbers = new ArrayList<Integer>() {{
        add(datasets);
        add(projects);
        add(openProjects);
        add(samples);
        add(rawDataSize);
        add(CURRENTSTORAGESIZE);
    }};

    public Numbers() {

    }

    public List<Integer> getAllNumbers() {
        return allNumbers;
    }

    public int getDatasets() {
        return datasets;
    }

    public void setDatasets(int datasets) {
        this.datasets = datasets;
    }

    public int getProjects() {
        return projects;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public int getOpenProjects() {
        return openProjects;
    }

    public void setOpenProjects(int openProjects) {
        this.openProjects = openProjects;
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public int getRawDataSize() {
        return rawDataSize;
    }

    public void setRawDataSize(int rawDataSize) {
        this.rawDataSize = rawDataSize;
    }

    public int getCURRENTSTORAGESIZE() {
        return CURRENTSTORAGESIZE;
    }

}

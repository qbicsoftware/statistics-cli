package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import io.queries.utils.Helpers;
import io.queries.utils.lexica.OpenBisTerminology;
import logging.Log4j2Logger;
import logging.Logger;
import submodule.data.ChartConfig;
import submodule.lexica.ChartNames;

import java.util.*;

public class ProjectsTechnologiesQuery implements IQuery {

    private static final Logger logger = new Log4j2Logger(ProjectsTechnologiesQuery.class);

    private final double THRESOLD = 0.05; //TODO move this one to config

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private final Map<String, Object> results = new HashMap<>();

    public ProjectsTechnologiesQuery(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }


    @Override
    public Map<String, ChartConfig> query() {

        logger.info("Run projects-by-technologies query");

        clear();
        countProjectsByTechnolgy();
        removeMinorities();

        Map<String, ChartConfig> map = new HashMap<>();
        map.put(ChartNames.Projects_Technology.toString(), Helpers.generateChartConfig(results, "ProjectsTechnolgy","Projectcounts by Technologies"));

        return map;
    }

    private void countProjectsByTechnolgy() {
        retrieveSamplesFromOpenBis().getObjects().forEach(sample ->{

            sample.getChildren().forEach(omicsChild -> {

                if(isOmicsRun(omicsChild.getType().toString())) {
                    Helpers.addEntryToStringCountMap(results, getOmicsName(omicsChild.getType().toString()), 1);
                }

            });
        });
    }

    private boolean isOmicsRun(String name){
        String[] array = name.split("_");
        return array[array.length-1].equals("RUN") && !array[1].equals("WF");
    }

    private String getOmicsName(String name){
        return  name.split("_")[1];
    }


    private void removeMinorities() {

        int total = results.values().stream().mapToInt(r -> (int) r).sum();

        Set<String> removables = new HashSet<>();

        results.keySet().forEach(key -> {
            if((double)(int)results.get(key) / (double)(int)total < THRESOLD){
                removables.add(key);
            }
        });

        removables.forEach(results::remove);
    }

    private void clear(){
        results.clear();
    }

    private SearchResult<Sample> retrieveSamplesFromOpenBis() {

        SampleSearchCriteria sampleSourcesCriteria = new SampleSearchCriteria();
        sampleSourcesCriteria.withType().withCode().thatEquals(OpenBisTerminology.TEST_SAMPLE.toString());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren().withType();
        fetchOptions.withProperties();

        return v3.searchSamples(sessionToken, sampleSourcesCriteria, fetchOptions);
    }

}

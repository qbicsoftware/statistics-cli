package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import io.queries.utils.Helpers;
import io.queries.utils.lexica.SpaceBlackList;
import logging.Log4j2Logger;
import logging.Logger;
import submodule.data.ChartConfig;
import submodule.lexica.ChartNames;

import java.util.*;

/**
 * @author fhanssen
 */
public class ProjectsTechnologiesQuery extends AQuery {

    //TODO still waiting for feedback from PMs
    private static final Logger logger = new Log4j2Logger(ProjectsTechnologiesQuery.class);

    private final double THRESOLD = 0.05; //TODO move this one to config

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private  SearchResult<Space> searchResult;

    private final Map<String, Object> results = new HashMap<>();

    public ProjectsTechnologiesQuery(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }


    @Override
    public Map<String, ChartConfig> query() {

        logger.info("Run projects-by-technologies query");

        clear();

        retrieveSamplesFromOpenBis();
        removeBlacklistedSpaces();

        countProjectsByTechnolgy();
        removeMinorities();

        Map<String, ChartConfig> map = new HashMap<>();
        map.put(ChartNames.Projects_Technology.toString(), Helpers.generateChartConfig(results, "counts", "Project Counts"));
        System.out.println(results.toString());

        return map;
    }

    private void clear() {
        results.clear();
    }

    private void retrieveSamplesFromOpenBis() {

        //TODO this spits out a result verifiable with the testing version: check with chris
        SpaceSearchCriteria c  = new SpaceSearchCriteria();
        c.withCode();

        SpaceFetchOptions s = new SpaceFetchOptions();
        s.withSamples().withChildren().withType();

        searchResult = v3.searchSpaces(sessionToken, c, s);

    }

    @Override
    void removeBlacklistedSpaces() {

        List<Space> removables = new ArrayList<>();
        searchResult.getObjects().forEach(space -> {
            if (SpaceBlackList.getList().contains(space.getCode())) {
                removables.add(space);
            }
        });
        searchResult.getObjects().removeAll(removables);

        logger.info("Removed results from blacklisted OpenBis Spaces: " + SpaceBlackList.getList().toString());

    }

    private void countProjectsByTechnolgy() {
        searchResult.getObjects().forEach(space -> {
            space.getSamples().forEach(sample -> {
                sample.getChildren().forEach(omicsChild -> {

                    if (isOmicsRun(omicsChild.getType().toString())) {
                        Helpers.addEntryToStringCountMap(results, getOmicsName(omicsChild.getType().toString()), 1);
                    }

                });
            });
        });
    }

    private boolean isOmicsRun(String name) {
        String[] array = name.split("_");
        return array[array.length - 1].equals("RUN") && !array[1].equals("WF");
    }

    private String getOmicsName(String name) {
        return name.split("_")[1];
    }


    private void removeMinorities() {

        int total = results.values().stream().mapToInt(r -> (int) r).sum();

        Set<String> removables = new HashSet<>();

        results.keySet().forEach(key -> {
            if ((double) (int) results.get(key) / (double) (int) total < THRESOLD) {
                removables.add(key);
            }
        });

        removables.forEach(results::remove);
    }


}

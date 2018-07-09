package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import io.queries.utils.Helpers;
import io.queries.utils.lexica.OpenBisTerminology;
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


    private static final Logger logger = new Log4j2Logger(ProjectsTechnologiesQuery.class);

    private final double THRESOLD = 0.05; //TODO move this one to config

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private SearchResult<Space> searchResult;

    private final Map<String, Object> results = new HashMap<>();

    private final Map<String, Set<String>> projectCodeToType = new HashMap<>();

    public ProjectsTechnologiesQuery(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }


    @Override
    public Map<String, ChartConfig> query() {

        logger.info("Run projects-by-technologies query");

        clear();

        retrieveDataFromOpenBis();
        //      removeBlacklistedSpaces();

        //     countProjectsByTechnolgy();
        //    removeMinorities();
//
        Map<String, ChartConfig> map = new HashMap<>();
        //       map.put(ChartNames.Projects_Technology.toString(), Helpers.generateChartConfig(results, "counts", "Project Counts"));
//        System.out.println(results.toString());

        return map;
    }

    private void clear() {
        results.clear();
    }

    private void retrieveDataFromOpenBis() {

        SpaceSearchCriteria c = new SpaceSearchCriteria();

        SpaceFetchOptions s = new SpaceFetchOptions();
        s.withSamples().withChildren().withType();


        searchResult = v3.searchSpaces(sessionToken, c, s);



        System.out.println(results);
    }

    private void addSpaceTechCount(Set<String> omicsType) {

        String curr;
        if(omicsType.size() > 1){
            curr = "Multi-omics";
        }else if(omicsType.size() == 1){
            curr = omicsType.iterator().next();
        }else{
            curr = "Unknown";
        }

        Helpers.addEntryToStringCountMap(results, curr, 1);

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

    private void countSpacesByTechnology() {

        searchResult.getObjects().forEach(space -> {
            Set<String> omicsType = new HashSet<>();
            space.getSamples().forEach(sample -> {


                sample.getChildren().forEach(omicsChild -> {

                    if(omicsChild.getType().toString().split("_").length > 1) {

                        if (!omicsChild.getType().toString().split("_")[1].equals("WF") && omicsChild.getType().toString().split("_")[omicsChild.getType().toString().split("_").length - 1].equals("RUN")) {
                            omicsType.add(omicsChild.getType().toString().replace("SampleType ", ""));
                        }
                    }

                });

            });
            addSpaceTechCount(omicsType);
        });
    }

    private boolean isOmicsRun(String name) {
        String[] array = name.split("_");
        return array[array.length - 1].equals("RUN") && !array[1].equals("WF");
    }

    private String getOmicsName(String name) {
        return name.split("_")[1];
    }



}

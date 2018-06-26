package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
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

    private  SearchResult<Space> searchResult;

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

        retrieveSamplesFromOpenBis();
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

    private void retrieveSamplesFromOpenBis() {

        //TODO this spits out a result verifiable with the testing version: check with chris

        //Bug in open bis? can't seem to connect projects with samples
        SpaceSearchCriteria c  = new SpaceSearchCriteria();
        c.withCode();

        SpaceFetchOptions s = new SpaceFetchOptions();
        //s.withProjects();

        ProjectFetchOptions test = new ProjectFetchOptions();
        test.withSamples();

        s.withProjectsUsing(test);
//        s.withSamples();
//        s.withProjects().withSamples(); //This does not work
//        s.withSamples().withChildren().withType();
//        s.withSamples().withProjectUsing(new ProjectFetchOptions()); // This does not work

        searchResult = v3.searchSpaces(sessionToken, c, s);

        List<String> projects = new ArrayList<>();
        searchResult.getObjects().forEach(space -> {
            //System.out.println(space.getCode());
            space.getProjects().forEach(project -> {
                projects.add(project.getCode());
                project.getSamples();
//                //System.out.println(project.getCode());
////                project.getSamples().forEach(sample -> {
////                    samples.add(sample.getCode());
////                });
//
//
           });

//            space.getSamples().forEach(sample -> {
//                //sample.getProject();
//            });
//            //System.out.println(space.getProjects());
        });
        System.out.println(projects.size());

        ProjectSearchCriteria p = new ProjectSearchCriteria();
//        p.withCode();
//
        ProjectFetchOptions pf = new ProjectFetchOptions();
        pf.withSamples(); //This does not work
//        pf.withSamples().withType();
//
//
////        System.out.println( v3.searchProjects(sessionToken, p, pf).getTotalCount());
////
        SearchResult<Project> r =  v3.searchProjects(sessionToken, p, pf);
//        System.out.println(r.getTotalCount());
//
        r.getObjects().forEach(project -> {
            project.getCode();
//            project.getSamples().forEach(sample -> {
//                sample.getType().toString();
//            });
        });

        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();

        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withProjectUsing(new ProjectFetchOptions()); // this does not work
        sampleFetchOptions.withSpace();
        sampleFetchOptions.withChildren().withType();

        SearchResult<Sample> sampleSearchResult = v3.searchSamples(sessionToken, sampleSearchCriteria, sampleFetchOptions);
        System.out.println(sampleSearchResult.getTotalCount());

        Set<String> spaces = new HashSet<>();
        sampleSearchResult.getObjects().forEach(sample -> {
            spaces.add(sample.getSpace().getCode());
        });

        System.out.println(spaces.size());

    }

    public Set<String> findDuplicates(List<String> listContainingDuplicates) {

        final Set<String> setToReturn = new HashSet<String>();
        final Set<String> set1 = new HashSet<String>();

        for (String yourInt : listContainingDuplicates) {
            if (!set1.add(yourInt)) {
                setToReturn.add(yourInt);
            }
        }
        return setToReturn;
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

    private void countProjectsByTechnology() {

        searchResult.getObjects().forEach(space -> {
            space.getSamples().forEach(sample -> {
                System.out.println(sample.getProject().getCode());

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

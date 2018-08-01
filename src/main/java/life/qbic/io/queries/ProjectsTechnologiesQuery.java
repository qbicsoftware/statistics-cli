package life.qbic.io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import life.qbic.io.queries.utils.Helpers;
import life.qbic.io.queries.utils.lexica.OmicsType;
import life.qbic.io.queries.utils.lexica.OpenBisTerminology;
import life.qbic.io.queries.utils.lexica.SpaceBlackList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import submodule.data.ChartConfig;
import submodule.lexica.ChartNames;

import java.util.*;

/**
 * @author fhanssen
 */
public class ProjectsTechnologiesQuery extends AQuery {


    private static final Logger logger = LogManager.getLogger(ProjectsTechnologiesQuery.class);

    private final double THRESOLD = 0.05; //TODO move this one to config

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private SearchResult<Sample> searchResult;

    private final Map<String, Object> resultsProjectCounts = new HashMap<>();

    private final Map<String, Set<String>> projectCodeToType = new HashMap<>();
    private final Map<Set<String>, Integer> multiOmicsCount = new HashMap<>();

    public ProjectsTechnologiesQuery(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }


    @Override
    public Map<String, ChartConfig> query() {

        logger.info("Run projects-by-technologies query");

        clear();

        retrieveDataFromOpenBis();
        removeBlacklistedSpaces();

        createProjectcodeTypeMap();
        countProjectsByType();

        Map<String, ChartConfig> map = new HashMap<>();

        Map<String, Object> temp = new HashMap<>();
        temp.put("Multiomics", multiOmicsCount);
        map.put(ChartNames.Projects_Technology.toString(), Helpers.generateChartConfig(resultsProjectCounts, "counts", "Project Counts"));
        map.put("Multiomics Count", Helpers.generateChartConfig(temp, "Multiomics count", "Multiomics count"));
//      System.out.println(resultsProjectCounts.toString());

        return map;
    }

    private void clear() {
        resultsProjectCounts.clear();
    }

    private void retrieveDataFromOpenBis() {

        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        sampleSearchCriteria.withType().withCode().thatEquals(OpenBisTerminology.TEST_SAMPLE.toString());

        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withChildren().withType();

        searchResult = v3.searchSamples(sessionToken, sampleSearchCriteria, sampleFetchOptions);

    }

    @Override
    void removeBlacklistedSpaces() {

        List<Sample> removables = new ArrayList<>();
        searchResult.getObjects().forEach(experiment -> {
            if (SpaceBlackList.getList().contains(experiment.getSpace().getCode())) {
                removables.add(experiment);
            }
        });
        searchResult.getObjects().removeAll(removables);
        logger.info("Removed resultsProjectCounts from blacklisted OpenBis Spaces: " + SpaceBlackList.getList().toString());

    }

    private void createProjectcodeTypeMap(){
        searchResult.getObjects().forEach(sample -> {

            Set<String> omicsType = new HashSet<>();

            //Determine omics type per sample
            sample.getChildren().forEach(c -> {
                if (!c.getType().toString().split("_")[1].equals("WF") && c.getType().toString().split("_")[c.getType().toString().split("_").length-1].equals("RUN")) {
                    omicsType.add(c.getType().toString().replace("SampleType ",""));
                }
            });

            String projectCode = sample.getCode().substring(0,5);
            if(projectCodeToType.containsKey(projectCode)){
                omicsType.addAll(projectCodeToType.get(projectCode));
            }
            projectCodeToType.put(projectCode, omicsType);
        });

        for(String p : projectCodeToType.keySet()){
            System.out.println(p + " --> " +projectCodeToType.get(p));
        }
    }

    private void countProjectsByType(){

        projectCodeToType.keySet().forEach(projectCode ->{
            if(projectCodeToType.get(projectCode).size() > 1){
                Helpers.addEntryToStringCountMap(resultsProjectCounts, OmicsType.MULTI_OMICS.toString(), 1);
                Helpers.addEntryToSetCountMap(multiOmicsCount, projectCodeToType.get(projectCode), 1);
            }else{
                Helpers.addEntryToStringCountMap(resultsProjectCounts, projectCodeToType.get(projectCode).iterator().next(), 1);
            }
        });
    }



//    private boolean isOmicsRun(String name) {
//        String[] array = name.split("_");
//        return array[array.length - 1].equals("RUN") && !array[1].equals("WF");
//    }
//
//    private String getOmicsName(String name) {
//        return name.split("_")[1];
//    }
//


}

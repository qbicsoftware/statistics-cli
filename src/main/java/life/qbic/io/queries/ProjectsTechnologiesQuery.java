package life.qbic.io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import life.qbic.exceptions.InvalidProjectCodeException;
import life.qbic.io.queries.utils.Helpers;
import life.qbic.io.queries.utils.lexica.OpenBisTerminology;
import life.qbic.io.queries.utils.lexica.SpaceBlackList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import submodule.data.ChartConfig;
import submodule.lexica.ChartNames;
import submodule.lexica.Other;
import submodule.lexica.Translator;

import java.util.*;

/**
 * @author fhanssen
 */
public class ProjectsTechnologiesQuery extends AQuery {


    private static final Logger logger = LogManager.getLogger(ProjectsTechnologiesQuery.class);

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
        removeBlacklistedSpaces();//TODO comment this back in
        // TODO however for testing I somehow only have access to chickenfarm stuff anymore, so it has to be commented out

        createProjectcodeTypeMap();
        countProjectsByType();

        Map<String, ChartConfig> map = new HashMap<>();

        Map<String, Object> mTemp = new HashMap<>();
        for (Set<String> set : multiOmicsCount.keySet()) {
            ArrayList<String> list = new ArrayList<>(set);
            String name = "";
            for (int i = 0; i < set.size() - 1; i++) {
                name = name.concat(Translator.getTranslation(list.get(i))).concat(" + ");
            }
            name = name.concat(Translator.getTranslation(list.get(list.size() - 1)));
            mTemp.put(name, multiOmicsCount.get(set));
        }

        map.put(ChartNames.Projects.toString(), Helpers.addPercentages(Helpers.generateChartConfig(resultsProjectCounts, "project", "Project Counts with Measured Samples", "Projects")));
        map.put(ChartNames.Project_Multi_omics.toString(), Helpers.generateChartConfig(mTemp, "multiomics", "Multiomics count", "Projects"));

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
        sampleFetchOptions.withSpace();

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

    private void createProjectcodeTypeMap() {
        searchResult.getObjects().forEach(sample -> {

            Set<String> omicsType = new HashSet<>();

            //Determine omics type per sample
            sample.getChildren().forEach(c -> {
                if (isOmicsRun(c.getType().toString())) {
                    omicsType.add(c.getType().toString().replace("SampleType ", ""));
                }
            });

            try{
                matchProjectCodeToType(sample.getCode(), omicsType);
            }catch(InvalidProjectCodeException e){
                logger.error("Query " + this.getClass() + ":" + e.getMessage());
                logger.error(e.getStackTrace());

            }
        });

    }

    private void matchProjectCodeToType(String code, Set<String> omicsType) throws InvalidProjectCodeException{
        if (code.length() > 5) {
            String projectCode = code.substring(0, 5);
            if (projectCodeToType.containsKey(projectCode)) {
                omicsType.addAll(projectCodeToType.get(projectCode));
            }
            projectCodeToType.put(projectCode, omicsType);
        } else {
            throw new InvalidProjectCodeException("Sample code is too short: " + code + ". At least 5 characters are expected in order to comply with sample code pattern of " +
                    "<projectcode>.concat(<sampleidentifier>).");
        }
    }

    private void countProjectsByType() {

        projectCodeToType.keySet().forEach(projectCode -> {
            if (projectCodeToType.get(projectCode).size() > 1) {

                Helpers.addEntryToStringCountMap(resultsProjectCounts, Translator.Multi_omics.getOriginal(), 1);

                Helpers.addEntryToSetCountMap(multiOmicsCount, projectCodeToType.get(projectCode), 1);

            } else if (projectCodeToType.get(projectCode).size() == 1) {
                Helpers.addEntryToStringCountMap(resultsProjectCounts, projectCodeToType.get(projectCode).iterator().next(), 1);
            }
            //else{
            //If unknown ignore
            //}
        });
    }


    private boolean isOmicsRun(String name) {
        String[] array = name.split("_");
        return array[array.length - 1].equals("RUN") && !array[1].equals("WF");
    }


}

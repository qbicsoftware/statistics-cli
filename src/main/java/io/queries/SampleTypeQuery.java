package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import io.queries.utils.Helpers;
import io.queries.utils.lexica.OpenBisTerminology;
import io.queries.utils.lexica.SpaceBlackList;
import logging.Log4j2Logger;
import logging.Logger;
import submodule.data.ChartConfig;
import submodule.lexica.ChartNames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fhanssen
 */
public class SampleTypeQuery extends AQuery {

    private static final Logger logger = new Log4j2Logger(SampleTypeQuery.class);

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private final Map<String, Object> results = new HashMap<>();

    private SearchResult<Sample> searchResult;


    public SampleTypeQuery(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }

    @Override
    public Map<String, ChartConfig> query() {

        logger.info("Run sample type query");

        clear();

        retrieveSamplesFromOpenBis();
        removeBlacklistedSpaces();

        countSampleTypes();

        Map<String, ChartConfig> map = new HashMap<>();
        map.put(ChartNames.Sample_Types.toString(), Helpers.generateChartConfig(results, "Samples", "Sample Type Counts"));

        return map;
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

        logger.info("Removed results from blacklisted OpenBis Spaces: " + SpaceBlackList.getList().toString());

    }

    private void countSampleTypes() {

        searchResult.getObjects().forEach(sample -> {
            if( sample.getProperties().get(OpenBisTerminology.SAMPLE_TYPE.toString()).toUpperCase().contains("RNA")){
                Helpers.addEntryToStringCountMap(results, "RNA", 1);     //TODO Lump all RNA together: Needs to be tested on big instance, since test instance only contains RNA
            }else {
                Helpers.addEntryToStringCountMap(results, sample.getProperties().get(OpenBisTerminology.SAMPLE_TYPE.toString()), 1);
            }
        });
    }

    private void clear(){
        results.clear();
    }

    private void retrieveSamplesFromOpenBis() {

        SampleSearchCriteria sampleSourcesCriteria = new SampleSearchCriteria();
        sampleSourcesCriteria.withType().withCode().thatEquals(OpenBisTerminology.TEST_SAMPLE.toString());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSpace();


        searchResult =  v3.searchSamples(sessionToken, sampleSourcesCriteria, fetchOptions);

    }


}

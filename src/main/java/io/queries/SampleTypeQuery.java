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

import java.util.HashMap;
import java.util.Map;

public class SampleTypeQuery implements IQuery {

    private static final Logger logger = new Log4j2Logger(SampleTypeQuery.class);

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private final Map<String, Object> results = new HashMap<>();

    public SampleTypeQuery(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }

    @Override
    public Map<String, ChartConfig> query() {

        logger.info("Run sample type query");

        clear();
        countSampleTypes();

        Map<String, ChartConfig> map = new HashMap<>();
        map.put(ChartNames.Sample_Types.toString(), Helpers.generateChartConfig(results, "Samples", "Sample Types"));

        return map;
    }

    private void countSampleTypes() {

        SearchResult<Sample> testSamples = retrieveSamplesFromOpenBis();

        testSamples.getObjects().forEach(sample ->
            Helpers.addEntryToStringCountMap(results,sample.getProperties().get(OpenBisTerminology.SAMPLE_TYPE.toString()), 1 )
        );
    }

    private void clear(){
        results.clear();
    }

    private SearchResult<Sample> retrieveSamplesFromOpenBis() {

        SampleSearchCriteria sampleSourcesCriteria = new SampleSearchCriteria();
        sampleSourcesCriteria.withType().withCode().thatEquals(OpenBisTerminology.TEST_SAMPLE.toString());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        return v3.searchSamples(sessionToken, sampleSourcesCriteria, fetchOptions);

    }


}

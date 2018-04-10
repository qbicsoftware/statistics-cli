package io.queries.numbersQuery;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import io.queries.IQuery;
import io.queries.utils.lexica.SpaceBlackList;
import logging.Log4j2Logger;
import logging.Logger;
import model.data.ChartConfig;
import model.data.ChartSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Heumos
 * This Query class provides factual numbers about our openBIS projects. Some of those such as storage size have to updated manually!
 */
public class NumbersQuery implements IQuery {

    private static final Logger logger = new Log4j2Logger(NumbersQuery.class);

    private final IApplicationServerApi v3;
    private final String sessionToken;

    //TODO we could also log when the numbers class had been modified the last time
    private Numbers numbers;

    public NumbersQuery(IApplicationServerApi v3, String sessionToken) {
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }

    private SearchResult<Sample> retrieveNumberOfProjects() {
        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        sampleSearchCriteria.withSpace();

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace(); //do we need this a second time?

        return v3.searchSamples(sessionToken, sampleSearchCriteria, fetchOptions);
    }

    private SearchResult<Sample> filterSamplesForBlackList(SearchResult<Sample> samples) {
        samples.getObjects().forEach(sample -> {
            if (SpaceBlackList.getList().contains(sample.getSpace().getCode())) {
                samples.getObjects().remove(sample);
            }
        });

        return samples;
    }


    @Override
    public Map<String, ChartConfig> query() {
        logger.info("Running numbers query");
        logger.info("Retrieving number of projects");
        SearchResult<Sample> samples = filterSamplesForBlackList(retrieveNumberOfProjects());
        numbers.setProjects(samples.getObjects().size()); //could also work with samples.getTotalCount(), but doc is not clear here

        //TODO get the rest of the numbers



        Map<String, ChartConfig> result = new HashMap<>();

        numbers.getAllNumbers().forEach(number -> {
            result.put(String.valueOf(number), generateChartConfig(number, String.valueOf(number)));
        });

        return null;
    }

    private ChartConfig generateChartConfig(Integer number, String title) {
        logger.info("Generating ChartConfig for: " + title);
        ChartSettings chartSettings = new ChartSettings(title);
        ChartConfig chartConfig = new ChartConfig();
        chartConfig.setSettings(chartSettings);

        Map<Object, ArrayList<Object>> data = new HashMap<>();

        //Name of the number + Number -> data
        data.put(String.valueOf(number), new ArrayList<>(number));

        chartConfig.setData(data);

        return chartConfig;
    }
}

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
public class SampleTypeQuery extends AQuery {

    private static final Logger logger = LogManager.getLogger(SampleTypeQuery.class);

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private final Map<String, Object> result = new HashMap<>();

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
        removeBlacklistedSpaces(); //TODO comment this back in
        // TODO however for testing I somehow only have access to chickenfarm stuff anymore, so it has to be commented out

        countSampleTypes();

        Map<String, ChartConfig> map = new HashMap<>();
        map.put(ChartNames.Sample_Types.toString(),Helpers.generateChartConfig(result, "Samples", "Number of Already Measured Samples"));

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

            Set<String> omicsType = new HashSet<>();

            //Determine omics type per sample
            sample.getChildren().forEach(c -> {
                if (!c.getType().toString().split("_")[1].equals("WF") && c.getType().toString().split("_")[c.getType().toString().split("_").length-1].equals("RUN")) {
                    //TODO reactivate the methods here
                    omicsType.add(c.getType().toString().replace("SampleType ",""));
                }
            });

            if(sample.getProperties().get(OpenBisTerminology.SAMPLE_TYPE.toString()).toUpperCase().contains("RNA")){
                //TODO Lump all RNA together: Needs to be tested on big instance, since test instance only contains RNA
                addSampleTechCount("RNA", omicsType);
            }else {
                addSampleTechCount(sample.getProperties().get(OpenBisTerminology.SAMPLE_TYPE.toString()), omicsType);
            }
        });
    }

    private void addSampleTechCount(String sampleType, Set<String> omicsType) {
        Map<String, Integer> temp = new HashMap<>();

        String curr = "";
        if(omicsType.size() > 1){
            curr = OmicsType.MULTI_OMICS.toString();
        }else if(omicsType.size() == 1){
            curr = omicsType.iterator().next();
        }

        if(!curr.isEmpty()) {// if empty, then sample has not been measured("Unknown")
            // : then don't do anything as we will ignore unmeasured samples.

            if (result.keySet().contains(curr)) {
                temp = (Map) result.get(curr);
            }
            Helpers.addEntryToStringCountMap(temp, sampleType, 1);
            result.put(curr, temp);

        }

    }

    private void clear(){
        result.clear();
    }

    private void retrieveSamplesFromOpenBis() {

        SampleSearchCriteria sampleSourcesCriteria = new SampleSearchCriteria();
        sampleSourcesCriteria.withType().withCode().thatEquals(OpenBisTerminology.TEST_SAMPLE.toString());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withChildren().withType();
        fetchOptions.withSpace();


        searchResult =  v3.searchSamples(sessionToken, sampleSourcesCriteria, fetchOptions);

    }


}

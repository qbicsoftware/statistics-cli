package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import io.queries.utils.Helpers;
import io.queries.utils.lexica.ChartNames;
import io.queries.utils.lexica.OpenBisTerminology;
import io.queries.utils.lexica.SpaceBlackList;
import io.queries.utils.lexica.SuperKingdoms;
import io.webservice.REST;
import logging.Log4j2Logger;
import logging.Logger;
import model.data.ChartConfig;
import model.data.ChartSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fhanssen
 * This Query class, accesses OpenBis and retrieves all samples. They are then counted once by the superkingdom they belong to,
 * once by genus and once by species. Species, which occupy large parts(> threshold, currently 25%) of a superkingdom,
 * are shown in the superkingdom resolution.
 */
public class OrganismCountQuery extends AQuery {

    private static Logger logger = new Log4j2Logger(OrganismCountQuery.class);


    private final String ncbiTaxanomyRestUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=";

    private final List<String> largeSpecies = new ArrayList<>(); //Species >= largeThreshold share
    private final double largeThreshold = 25.0;

    private final Map<String, String> vocabularyMap = new HashMap<>(); //511145=Escherichia coli strain K12 MG1655 [NCBI ID = Name]

    private final Map<String, Integer> organismCountMap = new HashMap<>(); //103690=26 [NCBI ID = count]
    private final Map<String, String> organismDomainMap = new HashMap<>(); //103690=Bacteria [NCBI ID = domain]
    private final Map<String, String> organismGenusMap = new HashMap<>(); //103690=Nostoc [NCBI ID = Genus]

    //Result maps
    private final Map<String, Object> domainCountMap = new HashMap<>(); // Eukaryota=2000, Bacteria = 200, Homo Sapiens = 5000,...
    private final Map<String, Map<String, Object>> genusCountMaps = new HashMap<>(); //Eukaryota, Map[Mus = 26]
    private final Map<String, Map<String, Object>> speciesCountMaps = new HashMap<>(); //Eukaryota, Map[Mus musculus = 23]

    private final Map<String, Object> organismNameGenusMap = new HashMap<>(); //Cavia aperea=Cavia [Species name = Genus]

    public OrganismCountQuery(IApplicationServerApi v3, String sessionToken) {
        super(v3, sessionToken);
    }

    @Override
    public Map<String, ChartConfig> query() {

        logger.info("Run organism count query");

        clearMaps();

        logger.info("Retrieve names of taxonomy ids.");
        mapTaxonomyIDToName();

        logger.info("Count OpenBis samples on species basis.");
        countSamplesPerOrganism(retrieveSamplesFromOpenBis());

        logger.info("Map species to domain and genus");
        setOrganismToDomainAndGenusMap();

        logger.info("Count samples on domain basis");
        generateDomainCountMap();

        logger.info("Retrieve species with a larger share than " + largeThreshold + " in their domain.");
        filterForLargeOrganisms();

        for (String domain : domainCountMap.keySet()) {
            if (!(domain.equals("Other") || domain.equals("unclassified sequences"))
                    && SuperKingdoms.getList().contains(domain)) { //exclude species with large share, which were added to superkingdom resolution from being further classified
                logger.info("Create genus and species count map of " + domain);
                genusCountMaps.put(domain, new HashMap<>());
                speciesCountMaps.put(domain, new HashMap<>());
            }
        }

        generateGenusCountMap();
        generateSpeciesCountMap();

        Map<String, ChartConfig> result = new HashMap<>();

        logger.info("Set results.");

        //Add Superkingdom to config
        result.put("SuperKingdom", generateChartConfig(domainCountMap, "SuperKingdom", "Sample Count by Domain"));

        //Add Genus maps to config
        for (String domain : genusCountMaps.keySet()) {
            result.put(domain.concat("_Genus"), generateChartConfig(genusCountMaps.get(domain), domain, "Sample Count".concat(domain)));
        }
        //Add Species to config
        for (String domain : speciesCountMaps.keySet()) {
            result.put(domain.concat("_Species"), generateChartConfig(speciesCountMaps.get(domain), domain, ""));
        }

        //Add species to genus map
        result.put(ChartNames.Species_Genus.toString(), generateChartConfig(organismNameGenusMap, ChartNames.Species_Genus.toString(), ""));

        return result;
    }

    private void clearMaps() {
        domainCountMap.clear();
        organismCountMap.clear();
        organismDomainMap.clear();
        organismGenusMap.clear();
        genusCountMaps.clear();
        speciesCountMaps.clear();
    }

    private SearchResult<Sample> retrieveSamplesFromOpenBis() {

        SampleSearchCriteria sampleSourcesCriteria = new SampleSearchCriteria();
        sampleSourcesCriteria.withSpace();
        sampleSourcesCriteria.withType().withCode().thatEquals(OpenBisTerminology.BIO_ENTITY.get());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProject();
        fetchOptions.withSpace();
        fetchOptions.withProperties();

        SearchResult<Sample> sampleSources = super.getV3().searchSamples(super.getSessionToken(), sampleSourcesCriteria, fetchOptions);

        return sampleSources;
    }

    private void countSamplesPerOrganism(SearchResult<Sample> sampleSources) {
        //Iterate over all search results
        for (Sample experiment : sampleSources.getObjects()) {
            //If sample does not belong to a blacklisted space, then increment its organism count
            if (!SpaceBlackList.getList().contains(experiment.getSpace().getCode())) {
                Helpers.addEntryToStringCountMap(organismCountMap, experiment.getProperties().get(OpenBisTerminology.NCBI_ORGANISM.get()), 1);
            }
        }
    }

    private void setOrganismToDomainAndGenusMap() {
        for (String organism : organismCountMap.keySet()) {
            if (organism.equals("0")) { //0 = 'Other' in domain and can't be queried to NCBI
                organismDomainMap.put(organism, "Other");
            } else {
                retrieveSuperKingdomAndGenusFromNCBI(organism);
            }
        }
    }

    private void retrieveSuperKingdomAndGenusFromNCBI(String organism) {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader((REST.call(ncbiTaxanomyRestUrl.concat(organism).concat("&retmode=xml")))))) {
            String line;
            String scientificName = "";
            while ((line = rd.readLine()) != null) {
                //Retrieve Genus
                if (line.contains("<Rank>genus")) {
                    organismGenusMap.put(organism, getScientificName(scientificName));
                }

                //Retrieve Superkingdom
                if (line.contains("<Rank>superkingdom")) {
                    organismDomainMap.put(organism, getScientificName(scientificName));
                }

                //Handle unclassified samples
                if (line.contains("<Lineage>unclassified sequences</Lineage>")) {
                    organismDomainMap.put(organism, "unclassified sequences");
                }

                if (line.contains("<ScientificName>")) {
                    scientificName = line.trim();
                }
            }
        } catch (IOException e) {
            logger.error("NCBI access to retrieve names of tax ids failed: " + e.getMessage());
        }
    }

    private String getScientificName(String line) {
        //Extract Scientific name with regex
        Pattern p = Pattern.compile("(<ScientificName>)(.*?)(<\\/ScientificName>)");
        Matcher m = p.matcher(line);
        m.find();

        return m.group(2);
    }

    private void generateDomainCountMap() {
        organismDomainMap.keySet().forEach(o ->
                Helpers.addEntryToStringCountMap(domainCountMap, organismDomainMap.get(o), organismCountMap.get(o)));

//        for (String organism : organismDomainMap.keySet()) {
//            Helpers.addEntryToStringCountMap(domainCountMap, organismDomainMap.get(organism), organismCountMap.get(organism));
//        }
    }

    private void filterForLargeOrganisms() {
        organismCountMap.keySet().forEach(o -> {
            double perc = 100.0 * (double) ((int) organismCountMap.get(o)) / (double) ((int) domainCountMap.get(organismDomainMap.get(o)));
            if (perc > largeThreshold && perc < 100.0) {
                largeSpecies.add(o);
                int currCount = (int) domainCountMap.get(organismDomainMap.get(o));
                domainCountMap.put(vocabularyMap.get(o), organismCountMap.get(o));
                domainCountMap.put(organismDomainMap.get(o), currCount - organismCountMap.get(o));
            }

        });
    }

    private void generateGenusCountMap() {

        for (String organism : organismGenusMap.keySet()) {
            if (!largeSpecies.contains(organism)) {
                if (SuperKingdoms.getList().contains(organismDomainMap.get(organism))) {
                    Helpers.addEntryToStringCountMap(genusCountMaps.get(organismDomainMap.get(organism)), organismGenusMap.get(organism), organismCountMap.get(organism));
                }
            }
        }
    }

    private void generateSpeciesCountMap() {

        for (String organism : organismDomainMap.keySet()) {
            organismNameGenusMap.put(vocabularyMap.get(organism), organismGenusMap.get(organism));
            if (!largeSpecies.contains(organism)) {
                if (SuperKingdoms.getList().contains(organismDomainMap.get(organism))) {
                    speciesCountMaps.get(organismDomainMap.get(organism)).put(vocabularyMap.get(organism), organismCountMap.get(organism));
                }
            }
        }
    }

    private void mapTaxonomyIDToName() {
        VocabularyTermFetchOptions vocabularyFetchOptions = new VocabularyTermFetchOptions();
        vocabularyFetchOptions.withVocabulary();

        VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
        vocabularyTermSearchCriteria.withCode();

        SearchResult<VocabularyTerm> vocabularyTermSearchResult = super.getV3().searchVocabularyTerms(super.getSessionToken(), vocabularyTermSearchCriteria, vocabularyFetchOptions);

        for (VocabularyTerm v : vocabularyTermSearchResult.getObjects()) {
            vocabularyMap.put(v.getCode(), v.getLabel());
        }
    }

    /**
     * Method turns retrieved data to a chart config. can be arbitrarily extended with more parameters. Be careful with adding data.
     * You somehow need to ensure that your data order matches the category order (here xCategories holds the names, data
     * holds the respective values, the need to be in the correct order to allow matching later!)
     *
     * @param result Map holding categories as key, and the respective values as values
     * @param name   Chart name
     * @param title  Chart title, stored in config and later displayed
     * @return ChartConfig
     */
    private ChartConfig generateChartConfig(Map<String, Object> result, String name, String title) {

        logger.info("Generate ChartConfig for: " + name);

        ChartConfig organismCount = new ChartConfig();

        //Add chart settings
        ChartSettings organismCountSettings = new ChartSettings(title);
        //Set xCategories
        List<String> organism = new ArrayList<>(result.keySet());

        organismCountSettings.setxCategories(new ArrayList<>(organism));

        //Add settings to chart config
        organismCount.setSettings(organismCountSettings);

        //Add chart data: be careful with order of data: must match xCategory order
        Map<Object, ArrayList<Object>> count = new HashMap<>();

        //This is necessary to always ensure proper order and mapping of key value pairs in YAML!
        ArrayList<Object> list = new ArrayList<>();
        organism.forEach(o -> list.add(result.get(o)));
        count.put(name, list);
        organismCount.setData(count);

        return organismCount;
    }

}


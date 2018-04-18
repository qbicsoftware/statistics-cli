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
import io.queries.utils.lexica.OpenBisTerminology;
import io.queries.utils.lexica.SpaceBlackList;
import io.webservice.REST;
import logging.Log4j2Logger;
import logging.Logger;
import submodule.data.ChartConfig;
import submodule.lexica.ChartNames;
import submodule.lexica.Kingdoms;

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

    private static final Logger logger = new Log4j2Logger(OrganismCountQuery.class);

    //TODO this should go into a config
    private final String NCBI_TAXONOMY_REST_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=";
    private final double THRESHOLD = 25.0; //threshold for when a fraction of species in a kingdom counts as large

    private final IApplicationServerApi v3;
    private final String sessionToken;

    private final List<String> largeSpecies = new ArrayList<>(); //Species >= largeThreshold share

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

        this.v3 = v3;
        this.sessionToken = sessionToken;
    }

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

        logger.info("Retrieve species with a larger share than " + THRESHOLD + " in their domain.");
        filterForLargeOrganisms();

        domainCountMap.keySet().forEach(domain -> {
            if (!(domain.equals("Other") || domain.equals("unclassified sequences"))
                    && Kingdoms.getList().contains(domain)) { //exclude species with large share, which were added to superkingdom resolution from being further classified
                logger.info("Create genus and species count map of " + domain);
                genusCountMaps.put(domain, new HashMap<>());
                speciesCountMaps.put(domain, new HashMap<>());
            }
        });

        generateGenusCountMap();
        generateSpeciesCountMap();

        Map<String, ChartConfig> result = new HashMap<>();

        logger.info("Set results.");

        //Add Superkingdom to config
        result.put("SuperKingdom", Helpers.generateChartConfig(domainCountMap, "SuperKingdom", "Sample Count by Domain"));

        //Add Genus maps to config
        genusCountMaps.keySet().forEach(domain ->
                result.put(domain.concat("_Genus"), Helpers.generateChartConfig(genusCountMaps.get(domain), domain, "Sample Count ".concat(domain))));
        //Add Species to config
        speciesCountMaps.keySet().forEach(domain ->
                result.put(domain.concat("_Species"), Helpers.generateChartConfig(speciesCountMaps.get(domain), domain, "")));

        //Add species to genus map
        result.put(ChartNames.Species_Genus.toString(), Helpers.generateChartConfig(organismNameGenusMap, ChartNames.Species_Genus.toString(), ""));

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
        sampleSourcesCriteria.withType().withCode().thatEquals(OpenBisTerminology.BIO_ENTITY.toString());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProject();
        fetchOptions.withSpace();
        fetchOptions.withProperties();

        return v3.searchSamples(sessionToken, sampleSourcesCriteria, fetchOptions);
    }

    private void countSamplesPerOrganism(SearchResult<Sample> sampleSources) {
        //Iterate over all search results
        sampleSources.getObjects().forEach(experiment -> {
            //If sample does not belong to a blacklisted space, then increment its organism count
            if (!SpaceBlackList.getList().contains(experiment.getSpace().getCode())) {
                Helpers.addEntryToStringCountMap(organismCountMap, experiment.getProperties().get(OpenBisTerminology.NCBI_ORGANISM.toString()), 1);
            }
        });
    }

    private void setOrganismToDomainAndGenusMap() {
        organismCountMap.keySet().forEach(organism -> {
            if (organism.equals("0")) { //0 = 'Other' in domain and can't be queried to NCBI
                organismDomainMap.put(organism, "Other");
            } else {
                retrieveSuperKingdomAndGenusFromNCBI(organism);
            }
        });
    }

    private void retrieveSuperKingdomAndGenusFromNCBI(String organism) {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader((REST.call(NCBI_TAXONOMY_REST_URL.concat(organism).concat("&retmode=xml")))))) {
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
    }

    private void filterForLargeOrganisms() {
        organismCountMap.keySet().forEach(o -> {
            double perc = 100.0 * (double) ((int) organismCountMap.get(o)) / (double) ((int) domainCountMap.get(organismDomainMap.get(o)));
            if (perc > THRESHOLD && perc < 100.0) {
                largeSpecies.add(o);
                int currCount = (int) domainCountMap.get(organismDomainMap.get(o));
                domainCountMap.put(vocabularyMap.get(o), organismCountMap.get(o));
                domainCountMap.put(organismDomainMap.get(o), currCount - organismCountMap.get(o));
            }

        });
    }

    private void generateGenusCountMap() {

        organismGenusMap.keySet().forEach(organism -> {
            if (!largeSpecies.contains(organism)) {
                if (Kingdoms.getList().contains(organismDomainMap.get(organism))) {
                    Helpers.addEntryToStringCountMap(genusCountMaps.get(organismDomainMap.get(organism)), organismGenusMap.get(organism), organismCountMap.get(organism));
                }
            }
        });
    }

    private void generateSpeciesCountMap() {

        organismDomainMap.keySet().forEach(organism -> {
            organismNameGenusMap.put(vocabularyMap.get(organism), organismGenusMap.get(organism));
            if (!largeSpecies.contains(organism)) {
                if (Kingdoms.getList().contains(organismDomainMap.get(organism))) {
                    speciesCountMaps.get(organismDomainMap.get(organism)).put(vocabularyMap.get(organism), organismCountMap.get(organism));
                }
            }
        });
    }

    private void mapTaxonomyIDToName() {
        VocabularyTermFetchOptions vocabularyFetchOptions = new VocabularyTermFetchOptions();
        vocabularyFetchOptions.withVocabulary();

        VocabularyTermSearchCriteria vocabularyTermSearchCriteria = new VocabularyTermSearchCriteria();
        vocabularyTermSearchCriteria.withCode();

        SearchResult<VocabularyTerm> vocabularyTermSearchResult = v3.searchVocabularyTerms(sessionToken, vocabularyTermSearchCriteria, vocabularyFetchOptions);

        vocabularyTermSearchResult.getObjects().forEach(v ->
                vocabularyMap.put(v.getCode(), v.getLabel())
        );
    }



}


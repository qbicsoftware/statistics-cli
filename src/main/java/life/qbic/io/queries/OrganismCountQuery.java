package life.qbic.io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import life.qbic.io.queries.utils.Helpers;
import life.qbic.io.queries.utils.lexica.OpenBisTerminology;
import life.qbic.io.queries.utils.lexica.SpaceBlackList;
import life.qbic.io.webservice.REST;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(OrganismCountQuery.class);

    private final IApplicationServerApi v3;
    private final String sessionToken;
    private final String ncbiTaxonomyRestUrl;
    private final double threshold; //threshold for when a fraction of species in a kingdom counts as large

    private  SearchResult<Sample> searchResult;

    private final List<String> largeSpecies = new ArrayList<>(); //Species >= largeThreshold share
    private final Set<String> largeDomains = new HashSet<>();

    private final Map<String, String> vocabularyMap = new HashMap<>(); //511145=Escherichia coli strain K12 MG1655 [NCBI ID = Name]

    private final Map<String, Integer> organismCountMap = new HashMap<>(); //103690=26 [NCBI ID = count]
    private final Map<String, String> organismDomainMap = new HashMap<>(); //103690=Bacteria [NCBI ID = domain]
    private final Map<String, String> organismGenusMap = new HashMap<>(); //103690=Nostoc [NCBI ID = Genus]

    //Result maps
    private final Map<String, Object> domainCountMap = new HashMap<>(); // Eukaryota=2000, Bacteria = 200, Homo Sapiens = 5000,...
    private final Map<String, Map<String, Object>> genusCountMaps = new HashMap<>(); //Eukaryota, Map[Mus = 26]
    private final Map<String, Map<String, Object>> speciesCountMaps = new HashMap<>(); //Eukaryota, Map[Mus musculus = 23]

    private final Map<String, Object> organismNameGenusMap = new HashMap<>(); //Cavia aperea=Cavia [Species name = Genus]

    public OrganismCountQuery(IApplicationServerApi v3, String sessionToken, String ncbiTaxUrl, double domainThreshold) {

        this.v3 = v3;
        this.sessionToken = sessionToken;
        this.ncbiTaxonomyRestUrl = ncbiTaxUrl;
        this.threshold = domainThreshold;
    }

    public Map<String, ChartConfig> query() {

        logger.info("Run organism count query");

        clearMaps();

        logger.info("Retrieve names of taxonomy ids.");
        mapTaxonomyIDToName();

        logger.info("Count OpenBis samples on species basis.");
        retrieveSamplesFromOpenBis();
        //removeBlacklistedSpaces();//TODO comment this back in
        // TODO however for testing I somehow only have access to chickenfarm stuff anymore, so it has to be commented out
        countSamplesPerOrganism();

        logger.info("Map species to domain and genus");
        setOrganismToDomainAndGenusMap();

        logger.info("Count samples on domain basis");
        generateDomainCountMap();

        logger.info("Retrieve species with a larger share than " + threshold + " in their domain.");
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
        largeDomains.forEach(largeDomain ->{
            domainCountMap.put("Other ".concat(largeDomain), domainCountMap.get(largeDomain));
        });
        largeDomains.forEach(largeDomain ->{
            domainCountMap.remove(largeDomain);
        });
        result.put("SuperKingdom", Helpers.addPercentages( Helpers.generateChartConfig(domainCountMap, "SuperKingdom", "Sample Count by Domain", "Organisms")));

        //Add Genus maps to config
        genusCountMaps.keySet().forEach(domain ->
                result.put(domain.concat("_Genus"), Helpers.addPercentages(Helpers.generateChartConfig(genusCountMaps.get(domain), domain, "Sample Count ".concat(domain), "Organisms"))));
        //Add Species to config
        speciesCountMaps.keySet().forEach(domain ->
                result.put(domain.concat("_Species"), Helpers.addPercentages(Helpers.generateChartConfig(speciesCountMaps.get(domain), domain, "", "Organisms"))));

        //Add species to genus map
        result.put(ChartNames.Species_Genus.toString(), Helpers.generateChartConfig(organismNameGenusMap, ChartNames.Species_Genus.toString(), "", "Organisms"));

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

    private void retrieveSamplesFromOpenBis() {

        SampleSearchCriteria sampleSourcesCriteria = new SampleSearchCriteria();
        sampleSourcesCriteria.withType().withCode().thatEquals(OpenBisTerminology.BIO_ENTITY.toString());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProject();
        fetchOptions.withSpace();
        fetchOptions.withProperties();
        searchResult = v3.searchSamples(sessionToken, sampleSourcesCriteria, fetchOptions);

    }

    @Override
    void  removeBlacklistedSpaces(){

        List<Sample> removables = new ArrayList<>();
        searchResult.getObjects().forEach(experiment -> {
            if (SpaceBlackList.getList().contains(experiment.getSpace().getCode())) {
                removables.add(experiment);
            }
        });
        searchResult.getObjects().removeAll(removables);

        logger.info("Removed results from blacklisted OpenBis Spaces: " + SpaceBlackList.getList().toString());

    }

    private void countSamplesPerOrganism() {
        //Iterate over all search results
        searchResult.getObjects().forEach(experiment -> {
            Helpers.addEntryToStringCountMap(organismCountMap, experiment.getProperties().get(OpenBisTerminology.NCBI_ORGANISM.toString()), 1);

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

    //FIXME: Check with Andreas when the new XML format is implemented
    private void retrieveSuperKingdomAndGenusFromNCBI(String organism) {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader((REST.call(ncbiTaxonomyRestUrl.concat(organism).concat("&retmode=xml")))))) {
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
            if (perc > threshold && perc < 100.0) {
                largeSpecies.add(o);
                int currCount = (int) domainCountMap.get(organismDomainMap.get(o));
                domainCountMap.put(vocabularyMap.get(o), organismCountMap.get(o));
                domainCountMap.put(organismDomainMap.get(o), currCount - organismCountMap.get(o));
                largeDomains.add(organismDomainMap.get(o));
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


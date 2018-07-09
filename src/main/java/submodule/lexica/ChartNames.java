package submodule.lexica;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author fhanssen
 * This enum class holds all usd chart names. Please extend it as new charts are added and sync it with the data portlet
 * to ensure everything is named correctly. Only change existing chart names if you know what you are doing.
 */
public enum ChartNames {
    SuperKingdom("SuperKingdom"),
    Eukaryota_Genus("Eukaryota_Genus"),
    Bacteria_Genus("Bacteria_Genus"),
    Viruses_Genus("Viruses_Genus"),
    Eukaryota_Species("Eukaryota_Species"),
    Bacteria_Species("Bacteria_Species"),
    Viruses_Species("Viruses_Species"),
    Species_Genus("Species_Genus"),
    Workflow_Execution_Counts("Workflow_Execution_Counts"),
    Available_Workflows_("Available_Workflows_"),
    Projects_Technology("Projects_Technology"),
    Sample_Types("Sample_Types");

    //TODO 1: Sync ChartNames with the eponymic class Chart names in the data retrieval tool


    private final String chartName;

    private static final List<String> enumList = Arrays.asList(Stream.of(ChartNames.values()).map(ChartNames::name).toArray(String[]::new));


    ChartNames(String chartName){
        this.chartName = chartName;
    }

    @Override
    public String toString(){
        return this.chartName;
    }

    public static List<String> getList(){
        return enumList;
    }
}

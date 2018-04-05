package io.queries.utils.lexica;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author fhanssen
 * This enum class holds all usd chart names. Please extend it as new charts are added and sync it with the statistics portlet
 * to ensure everything is named correctly. Only change existing chart names if you knwo what you are doing.
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
    GitHub("GitHub");
    //TODO 2: Add your chart names. Sync it with the statistic portlet ChartNames enum.



    private final String chartName;

    private static List<String> enumList = Arrays.asList(Stream.of(ChartNames.values()).map(ChartNames::name).toArray(String[]::new));

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

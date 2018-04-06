package io.queries.utils.lexica;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author fhanssen
 * This enum class holds spaces excluded from the analysis.
 */
public enum SpaceBlackList {
    CHICKEN_FARM,
    MNF_PCT_ARCHIVE;

    private static final List<String> enumList = Arrays.asList(Stream.of(SpaceBlackList.values()).map(SpaceBlackList::name).toArray(String[]::new));

    public static List<String> getList(){
        return enumList;
    }
}

package io.queries.utils.lexica;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fhanssen
 * This enum class holds spaces excluded from the analysis.
 */
public enum SpaceBlackList {
    CHICKEN_FARM,
    MNF_PCT_ARCHIVE;

    private static final List<String> enumList = Stream.of(SpaceBlackList.values())
                        .map(SpaceBlackList::name)
                        .collect(Collectors.toList());

    public static List<String> getList(){
        return enumList;
    }
}

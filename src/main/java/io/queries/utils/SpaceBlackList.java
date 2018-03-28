package io.queries.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SpaceBlackList {
    CHICKEN_FARM,
    MNF_PCT_ARCHIVE;

    private static List<String> valuesStringList = Stream.of(SpaceBlackList.values())
            .map(SpaceBlackList::name)
            .collect(Collectors.toList());

    public static List<String> getValuesStringList() {
        return valuesStringList;
    }
}

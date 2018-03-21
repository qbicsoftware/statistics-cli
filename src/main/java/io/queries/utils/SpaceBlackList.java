package io.queries.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum SpaceBlackList {
    CHICKEN_FARM,
    MNF_PCT_ARCHIVE;

    private static List<String> enumList = Arrays.asList(Stream.of(SpaceBlackList.values()).map(SpaceBlackList::name).toArray(String[]::new));

    public static List<String> getList(){
        return enumList;
    }
}

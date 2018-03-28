package io.queries.utils;

import java.util.Map;

public final class MapUtil {

    private MapUtil() {
        throw new UnsupportedOperationException("MapUtil is a utility class, do not create objects of it!");
    }

    public static void addEntryToStringCountMap(Map<String, Integer> map, String key, int count) {
        if (map.containsKey(key)) {
            int counter = map.get(key);
            map.put(key, counter + count);
        } else {
            map.put(key, count);
        }
    }
}

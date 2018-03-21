package io.queries.utils;

import java.util.Map;

public final class Helpers {

    public static void addEntryToStringCountMap(Map<String, Integer> map, String key, int count) {
        if (map.containsKey(key)) {
            int counter = map.get(key);
            map.put(key, counter + count);
        } else {
            map.put(key, count);
        }
    }
}

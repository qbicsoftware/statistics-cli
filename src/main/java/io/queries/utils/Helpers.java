package io.queries.utils;

import java.util.Map;
/**
 * @author fhanssen
 * This static class holds useful helper methods, that have to be reused for multiple queries.
 */
public final class Helpers {

    private Helpers() {
        throw new UnsupportedOperationException("Helpers is a utility class, do not create objects of it!");
    }

    public static void addEntryToStringCountMap(Map map, String key, int count) {
        if (map.containsKey(key)) {
            int counter = (int)map.get(key);
            map.put(key, counter + count);
        } else {
            map.put(key, count);
        }
    }
}

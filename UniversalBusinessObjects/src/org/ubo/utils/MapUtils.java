/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class MapUtils {

    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {

            @Override
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) {
                    return 1;
                } else {
                    return compare;
                }
            }
        };
        
        Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }
}

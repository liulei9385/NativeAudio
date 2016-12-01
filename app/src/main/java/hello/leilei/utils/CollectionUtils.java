/*
 * Copyright (c) 2016. Hefei Royalstar Electronic Appliance Group Co., Ltd. All rights reserved.
 */

package hello.leilei.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * USER: LEILEI
 * DATE: 2015/6/23
 * TIME: 14:33
 */
public class CollectionUtils {

    /**
     * default join separator
     **/
    public static final CharSequence DEFAULT_JOIN_SEPARATOR = ",";

    private CollectionUtils() {
        throw new AssertionError();
    }

    /**
     * is null or its size is 0
     * <p/>
     * <pre>
     * isEmpty(null)   =   true;
     * isEmpty({})     =   true;
     * isEmpty({1})    =   false;
     * </pre>
     *
     * @param <V>
     * @param c
     * @return if collection is null or its size is 0, return true, else return false.
     */
    public static <V> boolean isEmpty(Collection<V> c) {
        return (c == null || c.size() == 0);
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return (map == null || map.size() == 0);
    }

    /**
     * 集合是否为空
     *
     * @param tList
     * @param <T>
     * @return
     */
    public static <T> boolean isNotEmpty(List<T> tList) {
        return !isEmpty(tList);
    }

    /**
     * join collection to string, separator is {@link #DEFAULT_JOIN_SEPARATOR}
     * <p/>
     * <pre>
     * join(null)      =   "";
     * join({})        =   "";
     * join({a,b})     =   "a,b";
     * </pre>
     *
     * @param collection
     * @return join collection to string, separator is {@link #DEFAULT_JOIN_SEPARATOR}. if collection is empty, return
     * ""
     */
    public static String join(Iterable collection) {
        return collection == null ? "" : TextUtils.join(DEFAULT_JOIN_SEPARATOR, collection);
    }

    public static <V> boolean isEqual(Collection<V> a, Collection<V> b) {
        return !(a == null || b == null) && a.size() == b.size()
                && a.containsAll(b) && b.containsAll(a);
    }

    public static <K, V> boolean isEqual(Map<K, V> ma, Map<K, V> mb) {
        boolean isEqual;
        if ((ma == null && mb != null) ||
                (ma != null && mb == null))
            isEqual = false;
        else if (ma == mb) {
            isEqual = true;
        } else {
            isEqual = (ma.size() == mb.size());
            isEqual &= otherMap(ma, mb, null);
        }
        return isEqual;
    }

    public static <K, V> boolean isEqual(Map<K, V> ma,
                                         Map<K, V> mb,
                                         @Nullable Map<K, V> otherM) {
        boolean isEqual;
        if ((ma == null && mb != null) ||
                (ma != null && mb == null))
            isEqual = false;
        else if (ma == mb) {
            isEqual = true;
        } else {
            isEqual = (ma.size() == mb.size());
            isEqual &= otherMap(ma, mb, otherM);
        }
        return isEqual;
    }

    private static <K, V> boolean otherMap(
            Map<K, V> ma,
            Map<K, V> mb,
            @Nullable Map<K, V> otherM) {

        //集合a 集合b


        boolean isEqual;
        Set<Map.Entry<K, V>> maEn = ma.entrySet();
        Set<Map.Entry<K, V>> mbEn = mb.entrySet();
        isEqual = maEn.containsAll(mbEn) &&
                mbEn.containsAll(maEn);
        if (otherM != null) {

            for (Map.Entry<K, V> next : maEn) {
                if (!mb.containsValue(next.getValue())) {
                    otherM.put(next.getKey()
                            , next.getValue());
                }
            }

            for (Map.Entry<K, V> next : mbEn) {
                if (!ma.containsValue(next.getValue())) {
                    otherM.put(next.getKey()
                            , next.getValue());
                }
            }

        }

        return isEqual;
    }

    /**
     * 找出不同的object
     *
     * @param a
     * @param b
     * @param <V>
     * @return
     */
    public static <V> List<V> otherList(List<V> a, List<V> b) {
        List<V> list = null;
        if (!isEqual(a, b)) {
            Set<V> set = new HashSet<V>(5);
            set.addAll(a);
            set.addAll(b);
            if (set.size() == a.size()) {
                set.removeAll(b);
                list = new ArrayList<V>();
                list.addAll(set);
            } else {
                set.removeAll(a);
                list = new ArrayList<V>();
                list.addAll(set);
            }
        }
        return list;
    }

    public static <K, V> HashMap<K, V> emptyMap() {
        return new HashMap<K, V>();
    }

    public static void clearQuiletly(Collection<?>... collections) {
        for (Collection<?> coll : collections)
            if (coll != null) {
                coll.clear();
                coll = null;
            }
    }

    /**
     * 从map 安全去除数据
     *
     * @param map
     * @param key
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> V safeGetFromMap(Map<K, V> map, K key) {
        if (!isEmpty(map)) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

}
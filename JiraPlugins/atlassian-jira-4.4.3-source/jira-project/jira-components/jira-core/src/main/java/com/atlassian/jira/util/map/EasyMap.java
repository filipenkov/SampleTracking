package com.atlassian.jira.util.map;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * A replacement for UtilMisc.toMap().
 *
 * Most methods here are not null safe
 *
 * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
 */
public class EasyMap
{

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map build()
    {
        return createMap(1);
    }

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map build(final Object key1, final Object value1)
    {
        final Map map = createMap(1);

        map.put(key1, value1);

        return map;
    }

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map build(final Object key1, final Object value1, final Object key2, final Object value2)
    {
        final Map map = createMap(2);

        map.put(key1, value1);
        map.put(key2, value2);

        return map;
    }

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map build(final Object key1, final Object value1, final Object key2, final Object value2, final Object key3, final Object value3)
    {
        final Map map = createMap(3);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        return map;
    }

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map build(final Object key1, final Object value1, final Object key2, final Object value2, final Object key3, final Object value3, final Object key4, final Object value4)
    {
        final Map map = createMap(4);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);

        return map;
    }

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map build(final Object key1, final Object value1, final Object key2, final Object value2, final Object key3, final Object value3, final Object key4, final Object value4, final Object key5, final Object value5)
    {
        final Map map = createMap(5);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);

        return map;
    }

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map build(final Object key1, final Object value1, final Object key2, final Object value2, final Object key3, final Object value3, final Object key4, final Object value4, final Object key5, final Object value5, final Object key6, final Object value6)
    {
        final Map map = createMap(6);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);

        return map;
    }

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map build(final Object key1, final Object value1, final Object key2, final Object value2, final Object key3, final Object value3, final Object key4, final Object value4, final Object key5, final Object value5, final Object key6, final Object value6, final Object key7, final Object value7)
    {
        final Map map = createMap(7);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);

        return map;
    }

    /**
     * @deprecated since v3.13. Use {@link com.atlassian.jira.util.collect.MapBuilder} instead.
     */
    public static Map createMap(final int size)
    {
        return new HashedMap(size);
    }

}

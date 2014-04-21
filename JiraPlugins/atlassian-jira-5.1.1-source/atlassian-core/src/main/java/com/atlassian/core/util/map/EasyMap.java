package com.atlassian.core.util.map;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * A replacement for UtilMisc.toMap().
 * <p/>
 * Most methods here are not null safe
 */
public class EasyMap
{
    public static Map build()
    {
        return createMap(1);
    }

    public static Map build(Object key1, Object value1)
    {
        Map map = createMap(1);

        map.put(key1, value1);

        return map;
    }

    public static Map build(Object key1, Object value1, Object key2, Object value2)
    {
        Map map = createMap(2);

        map.put(key1, value1);
        map.put(key2, value2);

        return map;
    }

    public static Map build(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3)
    {
        Map map = createMap(3);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        return map;
    }

    public static Map build(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3,
            Object key4, Object value4)
    {
        Map map = createMap(4);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);

        return map;
    }

    public static Map build(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3,
            Object key4, Object value4, Object key5, Object value5)
    {
        Map map = createMap(5);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);

        return map;
    }

    public static Map build(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3,
            Object key4, Object value4, Object key5, Object value5, Object key6, Object value6)
    {
        Map map = createMap(6);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);

        return map;
    }

    public static Map build(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3,
            Object key4, Object value4, Object key5, Object value5, Object key6, Object value6, Object key7,
            Object value7)
    {
        Map map = createMap(7);

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);

        return map;
    }

    public static Map createMap(int size)
    {
        return new HashedMap(size);
    }
    
    /**
     * Takes a variable number of objects to build a map
     *
     * @throws RuntimeException if parameters are not even
     * @param objects
     * @return
     */
    public static Map build(Object... objects)
    {
        Map map = createMap(1);

        if (objects.length % 2 != 0)
        {
            throw new RuntimeException("The number of parameters should be even when building a map");
        }

        for (int i = 0; i < objects.length; i = (i + 2))
        {
            Object key = objects[i];
            Object value = objects[i+1];
            map.put(key, value);
        }

        return map;
    }
}

package com.atlassian.soy.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.restricted.BooleanData;
import com.google.template.soy.data.restricted.FloatData;
import com.google.template.soy.data.restricted.IntegerData;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;

import java.util.Arrays;
import java.util.Map;

/**
 * Converts a Map of general objects into a SoyMapData instance.
 *
 * @since v5.0
 */
public class SoyDataConverter
{
    public static <V> SoyMapData convertToSoyMapData(Map<String, V> map)
    {
        SoyMapData soyMapData = new SoyMapData();
        for (Map.Entry<String, V> entry : map.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            Object soyValue = convertObject(value);
            soyMapData.put(key, soyValue);
        }
        return soyMapData;
    }

    public static <V> SoyListData convertToSoyListData(Iterable<V> list)
    {
        SoyListData listData = new SoyListData();
        for (Object o : list)
        {
            listData.add(convertObject(o));
        }
        return listData;
    }

    public static SoyData convertToSoyData(Object value)
    {
        return SoyData.createFromExistingData(convertObject(value));
    }

    @SuppressWarnings ( { "unchecked" })
    static Object convertObject(Object value)
    {
        if (value == null
                || value instanceof Boolean
                || value instanceof Integer
                || value instanceof Double
                || value instanceof Float
                || value instanceof String)
        {
            return value;
        }
        else if (value instanceof Short
                || value instanceof Byte)
        {
            return ((Number) value).intValue();
        }
        else if (value instanceof Long)
        {
            final Long longValue = (Long) value;
            if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE)
            {
                return longValue.intValue();
            }
            else
            {
                // Could throw an exception... or make a best effort. Unlikely to happen?
                return longValue.doubleValue();
            }
        }
        else if (value instanceof Map)
        {
            return convertToSoyMapData((Map<String, Object>) value);
        }
        else if (value instanceof Iterable)
        {
            return convertToSoyListData((Iterable<Object>) value);
        }
        else if (value.getClass().isArray())
        {
            // Should we support primitive arrays?
            return convertToSoyListData(Arrays.asList((Object[]) value));
        }
        else if (value instanceof Enum)
        {
            return new EnumData((Enum) value);
        }
        else
        {
            // Some general object. Inspect it into a map, lazily
            return new LazySoyMapData(value);
        }
    }

    public static Object convertFromSoyData(SoyData data)
    {
        if (data instanceof LazySoyMapData)
        {
            return ((LazySoyMapData) data).getDelegate();
        }
        else if (data instanceof SoyMapData)
        {
            return Maps.transformValues(((SoyMapData) data).asMap(), new Function<SoyData, Object>()
            {
                @Override
                public Object apply(SoyData from)
                {
                    return convertFromSoyData(from);
                }
            });
        }
        else if (data instanceof SoyListData)
        {
            return Lists.transform(((SoyListData) data).asList(), new Function<SoyData, Object>()
            {
                @Override
                public Object apply(SoyData from)
                {
                    return convertFromSoyData(from);
                }
            });
        }
        else if (data instanceof EnumData)
        {
            return ((EnumData) data).getValue();
        }
        else if (data instanceof StringData)
        {
            return data.stringValue();
        }
        else if (data instanceof IntegerData)
        {
            return ((IntegerData) data).getValue();
        }
        else if (data instanceof BooleanData)
        {
            return ((BooleanData) data).getValue();
        }
        else if (data instanceof FloatData)
        {
            return ((FloatData) data).getValue();
        }
        else
        {
            return data == NullData.INSTANCE ? null : data.stringValue();
        }
    }

}
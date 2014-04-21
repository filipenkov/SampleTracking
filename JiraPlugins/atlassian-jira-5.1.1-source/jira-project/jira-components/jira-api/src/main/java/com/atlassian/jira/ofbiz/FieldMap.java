package com.atlassian.jira.ofbiz;

import com.atlassian.annotations.PublicApi;

import java.util.HashMap;

/**
 * Convenience class to use for field maps in {@link OfBizDelegator}
 *
 * @see com.atlassian.jira.ofbiz.OfBizDelegator
 * @since v4.0
 */
@PublicApi
public class FieldMap extends HashMap<String, Object>
{
    public FieldMap()
    {
        super();
    }

    public FieldMap(final String fieldName, final Object fieldValue)
    {
        super(1);
        put(fieldName, fieldValue);
    }

    /**
     * Adds the given key-value pair to the Map, and returns {@code this} in order to allow fluent syntax.
     *
     * <p> This method allows you to write code like the following:<br>
     * <code>
     * FieldMap fieldMap = new FieldMap("id", 12L).add("name", "Fred");
     * </code>
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return This FieldMap.
     */
    public FieldMap add(final String key, final Object value)
    {
        put(key, value);
        return this;
    }

    public static FieldMap build(final String fieldName, final Object fieldValue)
    {
        return new FieldMap(fieldName, fieldValue);
    }

    public static FieldMap build(String name1, Object value1, String name2, Object value2)
    {
        return new FieldMap(name1, value1).add(name2, value2);
    }

    public static FieldMap build(String name1, Object value1, String name2, Object value2, String name3, Object value3)
    {
        return new FieldMap(name1, value1).add(name2, value2).add(name3, value3);
    }

    public static FieldMap build(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4)
    {
        return new FieldMap(name1, value1).add(name2, value2).add(name3, value3).add(name4, value4);
    }
}

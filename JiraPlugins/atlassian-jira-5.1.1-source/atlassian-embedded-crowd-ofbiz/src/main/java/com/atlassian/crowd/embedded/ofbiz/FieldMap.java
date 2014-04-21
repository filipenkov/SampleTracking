package com.atlassian.crowd.embedded.ofbiz;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * Convienence class for working with Maps of Fields values for OfBiz
 * @since v4.2
 */
public class FieldMap extends HashMap<String, Object>
{
    public static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    public FieldMap()
    {
        super();
    }

    public FieldMap(final int initialCapacity)
    {
        super(initialCapacity);
    }

    public static Map<String, Object> build(final String fieldName, final Object value)
    {
        return new FieldMap().add(fieldName, value);
    }

    private Map<String, Object> add(final String fieldName, final Object value)
    {
        put(fieldName, value);
        return this;
    }
}

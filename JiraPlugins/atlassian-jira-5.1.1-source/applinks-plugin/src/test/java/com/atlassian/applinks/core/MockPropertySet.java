package com.atlassian.applinks.core;

import com.atlassian.applinks.api.PropertySet;

import java.util.HashMap;
import java.util.Map;

final class MockPropertySet implements PropertySet {

    private final Map<String, Object> map = new HashMap<String, Object>();

    public final Object getProperty(final String key) {
        return map.get(key);
    }

    public final Object putProperty(final String key, final Object value) {
        return map.put(key, value);
    }

    public final Object removeProperty(final String key) {
        return map.remove(key);
    }

}

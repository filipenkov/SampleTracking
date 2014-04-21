package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.impl.ImmutableAttributes;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds an Attributes implementation from OfBiz data.
 */
class OfBizAttributesBuilder
{
    static final String NAME = "name";
    static final String VALUE = "value";

    static Attributes toAttributes(final List<GenericValue> attributes)
    {
        if (attributes == null)
        {
            return new ImmutableAttributes();
        }
        final Map<String, Set<String>> attributesMap = Maps.newHashMap();
        for (final GenericValue attribute : attributes)
        {
            addAttribute(attributesMap, attribute);
        }
        return new ImmutableAttributes(attributesMap);
    }

    private static void addAttribute(final Map<String, Set<String>> attributesMap, final GenericValue attributeGv)
    {
        final String name = attributeGv.getString(NAME);
        final Set<String> values = attributesMap.get(name) != null ? attributesMap.get(name) : Sets.<String> newHashSet();
        // Convert null attributes to empty strings, because Oracle treats them as equivalent and returns empty strings.
        final String value = attributeGv.getString(VALUE);
        values.add(value == null ? "" : value);
        attributesMap.put(name, values);
    }
}

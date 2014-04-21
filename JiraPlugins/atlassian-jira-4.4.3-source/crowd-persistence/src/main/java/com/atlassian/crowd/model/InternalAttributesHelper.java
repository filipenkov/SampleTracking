package com.atlassian.crowd.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InternalAttributesHelper
{
    public <T extends InternalEntityAttribute> Map<String, Set<String>> attributesListToMap(Set<T> attributesList)
    {
        Map<String, Set<String>> attributesMap = new HashMap<String, Set<String>>();
        for (InternalEntityAttribute attribute : attributesList)
        {
            if (!attributesMap.containsKey(attribute.getName()))
            {
                attributesMap.put(attribute.getName(), new HashSet<String>());
            }
            attributesMap.get(attribute.getName()).add(attribute.getValue());
        }
        return attributesMap;
    }
}

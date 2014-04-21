package com.atlassian.jira.issue.transport;

import java.util.Collection;

/**
 * This is a field params with Lists as the value
 */
public interface CollectionParams extends FieldParams
{
    Collection getAllValues();

    Collection getValuesForNullKey();

    Collection getValuesForKey(String key);

    /**
     * Put the values in.
     *
     * @param key for mapping
     * @param value a Collection of Strings.
     */
    void put(String key, Collection<String> value);
}

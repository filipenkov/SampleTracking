package com.atlassian.jira.issue.transport;

import com.atlassian.annotations.PublicApi;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This is a field params with Lists as the value
 */
@PublicApi
public interface CollectionParams extends FieldParams
{
    Collection getAllValues();

    Collection getValuesForNullKey();

    Collection getValuesForKey(@Nullable String key);

    /**
     * Put the values in.
     *
     * @param key for mapping
     * @param value a Collection of Strings.
     */
    void put(String key, Collection<String> value);
}

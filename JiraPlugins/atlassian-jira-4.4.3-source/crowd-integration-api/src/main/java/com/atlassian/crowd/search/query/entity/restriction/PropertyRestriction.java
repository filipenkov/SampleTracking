package com.atlassian.crowd.search.query.entity.restriction;

import com.atlassian.crowd.embedded.api.SearchRestriction;

/**
 * Restriction on a search based on a property of type T.
 */
public interface PropertyRestriction<T> extends SearchRestriction
{
    /**
     * Returns the property to match on.
     *
     * @return Property object to match on.
     */
    Property<T> getProperty();

    /**
     * Returns the mode to match a property.
     *
     * @return match mode
     */
    MatchMode getMatchMode();

    /**
     * Returns the value to match against the property.
     *
     * @return value of type T to match against the property.
     */
    T getValue();
}

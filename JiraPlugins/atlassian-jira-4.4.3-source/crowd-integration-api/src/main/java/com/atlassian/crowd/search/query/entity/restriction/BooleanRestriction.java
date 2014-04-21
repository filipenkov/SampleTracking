package com.atlassian.crowd.search.query.entity.restriction;

import com.atlassian.crowd.embedded.api.SearchRestriction;

import java.util.Collection;

/**
 * A boolean search restriction.  Users of this interface will ensure that only items satisfying all the restrictions
 * returned by {@link #getRestrictions()} using the boolean logic returned by {@link #getBooleanLogic()} are returned.
 */
public interface BooleanRestriction extends SearchRestriction
{
    enum BooleanLogic
    {
        AND, OR
    }

    /**
     * Returns a collection of restrictions. Only items satisfying the restrictions using the boolean logic are returned.
     * I.e. if the list of restrictions are empty, then no results are returned.
     *
     * @see NullRestriction to implement no restrictions.
     *
     * @return a collection of <tt>SearchRestriction</tt>
     */
    Collection<SearchRestriction> getRestrictions();

    /**
     * Returns the boolean logic used against the collection of <tt>SearchRestriction</tt> returned by {@link #getRestrictions()}
     * to determine if an item should be included in a search result.
     *
     * @return boolean logic
     */
    BooleanLogic getBooleanLogic();
}

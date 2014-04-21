package com.atlassian.crowd.search.builder;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;

import java.util.*;

/**
 * Builder for MultiTermRestrictions.
 *
 * For usage see <code>QueryBuilder</code>.
 */
public class Combine
{
    /**
     * Returns an <tt>OR</tt> boolean search restriction where only one or more of the search restrictions have to be
     * satisfied.
     *
     * @param restrictions search restrictions
     * @return <tt>OR</tt> boolean search restriction
     */
    public static BooleanRestriction anyOf(SearchRestriction... restrictions)
    {
        return new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, restrictions);
    }

    /**
     * Returns an <tt>AND</tt> boolean search restriction where all of the search restrictions have to be satisfied.
     *
     * @param restrictions search restrictions
     * @return <tt>AND</tt> boolean search restriction
     */
    public static BooleanRestriction allOf(SearchRestriction... restrictions)
    {
        return new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, restrictions);
    }

    /**
     * Returns an <tt>OR</tt> boolean search restriction where only one or more of the search restrictions have to be
     * satisfied.
     *
     * @param restrictions search restrictions
     * @return <tt>OR</tt> boolean search restriction
     */
    public static BooleanRestriction anyOf(Collection<SearchRestriction> restrictions)
    {
        return new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, restrictions);
    }

    /**
     * Returns an <tt>AND</tt> boolean search restriction where all of the search restrictions have to be satisfied.
     *
     * @param restrictions search restrictions
     * @return <tt>AND</tt> boolean search restriction
     */
    public static BooleanRestriction allOf(Collection<SearchRestriction> restrictions)
    {
        return new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, restrictions);
    }
}

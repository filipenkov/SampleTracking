package com.atlassian.jira.entityengine;

import org.ofbiz.core.entity.EntityFindOptions;

/**
 * Entity options class with convenience builder methods.
 *
 * @since v5.1
 */
public class FindOptions extends EntityFindOptions
{
    /**
     * Creates a new FindOptions with no options set.
     *
     * @return a new FindOptions
     */
    public static FindOptions findOptions()
    {
        return new FindOptions();
    }

    private FindOptions()
    {
    }

    private FindOptions(boolean specifyTypeAndConcur, int resultSetType, int resultSetConcurrency, boolean distinct, int maxResults)
    {
        super(specifyTypeAndConcur, resultSetType, resultSetConcurrency, distinct, maxResults);
    }

    /**
     * Returns a new FindOptions with the given <code>specifyTypeAndConcur</code>.
     *
     * @param specifyTypeAndConcur
     * @return a new FindOptions
     */
    public FindOptions specifyTypeAndConcur(boolean specifyTypeAndConcur)
    {
        return new FindOptions(specifyTypeAndConcur, resultSetType, resultSetConcurrency, distinct, maxResults);
    }

    /**
     * Returns a new FindOptions with the given <code>resultSetType</code>.
     *
     * @param resultSetType
     * @return a new FindOptions
     */
    public FindOptions resultSetType(int resultSetType)
    {
        return new FindOptions(specifyTypeAndConcur, resultSetType, resultSetConcurrency, distinct, maxResults);
    }

    /**
     * Returns a new FindOptions with the given <code>resultSetConcurrency</code>.
     *
     * @param resultSetConcurrency
     * @return a new FindOptions
     */
    public FindOptions resultSetConcurrency(int resultSetConcurrency)
    {
        return new FindOptions(specifyTypeAndConcur, resultSetType, resultSetConcurrency, distinct, maxResults);
    }

    /**
     * Returns a new FindOptions with the given <code>distinct</code>.
     *
     * @param distinct
     * @return a new FindOptions
     */
    public FindOptions distinct(boolean distinct)
    {
        return new FindOptions(specifyTypeAndConcur, resultSetType, resultSetConcurrency, distinct, maxResults);
    }

    /**
     * Returns a new FindOptions with the given <code>maxResults</code>.
     *
     * @param maxResults
     * @return a new FindOptions
     */
    public FindOptions maxResults(int maxResults)
    {
        return new FindOptions(specifyTypeAndConcur, resultSetType, resultSetConcurrency, distinct, maxResults);
    }
}

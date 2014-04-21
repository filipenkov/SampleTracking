package com.atlassian.crowd.embedded.api;

/**
 * Represents a search query for user management. Typical queries will search for users, groups or user names, group
 * names.
 * @param <T> the type of element the query is expecting to return (eg. {@link com.atlassian.crowd.embedded.api.User}, {@link com.atlassian.crowd.embedded.api.Group}, etc.)
 */
public interface Query<T>
{
    /**
     * The index of the first element to return.
     * @return a positive index value.
     */
    int getStartIndex();

    /**
     * The maximum number of elements to return.
     * @return a number of elements.
     */
    int getMaxResults();

    /**
     * The type of elements to return.
     * @return the expected type of elements to be returned by the search.
     */
    Class<T> getReturnType();

    /**
     * Restrictions to apply to the query. Typically finding users of a given name, etc.
     * @return the search resctiction to apply to this query.
     */
    SearchRestriction getSearchRestriction();
}

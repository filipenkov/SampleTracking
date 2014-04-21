package com.atlassian.crowd.search.query.entity.restriction;

public enum MatchMode
{
    EXACTLY_MATCHES(true),
    STARTS_WITH(false),
    CONTAINS(false),
    LESS_THAN(false),
    GREATER_THAN(false),
    NULL(true);

    private final boolean exact;

    private MatchMode(boolean exact)
    {
        this.exact = exact;
    }

    /**
     * Returns true if this value represents an exact match mode. In other words,
     * this method returns true only if this match mode can never match with
     * more than one predetermined value.
     *
     * @return true if this match mode is exact
     */
    public boolean isExact()
    {
        return exact;
    }
}

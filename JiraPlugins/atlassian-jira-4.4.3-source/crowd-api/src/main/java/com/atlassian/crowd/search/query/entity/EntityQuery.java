package com.atlassian.crowd.search.query.entity;

import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class EntityQuery<T> implements Query<T>
{
    private final EntityDescriptor entityDescriptor;
    private final SearchRestriction searchRestriction;
    private final int startIndex;
    private final int maxResults;
    private Class<T> returnType;
    /**
     * This is the recommended maximum number of 'max' results the system will allow you to return. This value is
     * <strong>NOT</strong> enforced. ApplicationServiceGeneric often retrieves (startIndex + maxResults) number of
     * results which breaks this MAX_MAX_RESULTS.
     */
    public static final int MAX_MAX_RESULTS = 1000;

    /**
     * Flag to indicate that an EntityQuery should retrieve all results.
     * <p>
     * <strong>WARNING:</strong> using this flag could retrieve thousands or millions of entities. Misuse can cause
     * <em>massive performance problems</em>. This flag should only ever be used in exceptional circumstances.
     * <p>
     * If you need to find "all" entities, then consider making multiple successive calls to Crowd to receive
     * partial results. That way, the entire result set is never stored in memory on the Crowd server at
     * any one time.
     */
    public static final int ALL_RESULTS = -1;

    public EntityQuery(final Class<T> returnType, final EntityDescriptor entityDescriptor, final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
    {
        Validate.notNull(entityDescriptor, "entity cannot be null");
        Validate.notNull(searchRestriction, "searchRestriction cannot be null");
        Validate.notNull(returnType, "returnType cannot be null");
        Validate.isTrue(maxResults == ALL_RESULTS || maxResults > 0, "maxResults must be greater than 0 (unless set to EntityQuery.ALL_RESULTS)");
        Validate.isTrue(startIndex >= 0, "startIndex cannot be less than zero");

        this.entityDescriptor = entityDescriptor;
        this.searchRestriction = searchRestriction;
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.returnType = returnType;
    }

    public EntityQuery(final EntityQuery query, final Class<T> returnType)
    {
        this(returnType, query.getEntityDescriptor(), query.getSearchRestriction(), query.getStartIndex(), query.getMaxResults());
    }

    public EntityQuery(final EntityQuery<T> query, final int startIndex, final int maxResults)
    {
        this(query.getReturnType(), query.getEntityDescriptor(), query.getSearchRestriction(), startIndex, maxResults);
    }

    public EntityDescriptor getEntityDescriptor()
    {
        return entityDescriptor;
    }

    public SearchRestriction getSearchRestriction()
    {
        return searchRestriction;
    }

    public int getStartIndex()
    {
        return startIndex;
    }

    public int getMaxResults()
    {
        return maxResults;
    }

    public Class<T> getReturnType()
    {
        return returnType;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof EntityQuery))
        {
            return false;
        }

        EntityQuery query = (EntityQuery) o;

        if (maxResults != query.maxResults)
        {
            return false;
        }
        if (startIndex != query.startIndex)
        {
            return false;
        }
        if (entityDescriptor != null ? !entityDescriptor.equals(query.entityDescriptor) : query.entityDescriptor != null)
        {
            return false;
        }
        if (returnType != query.returnType)
        {
            return false;
        }
        //noinspection RedundantIfStatement
        if (searchRestriction != null ? !searchRestriction.equals(query.searchRestriction) : query.searchRestriction != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = entityDescriptor != null ? entityDescriptor.hashCode() : 0;
        result = 31 * result + (searchRestriction != null ? searchRestriction.hashCode() : 0);
        result = 31 * result + startIndex;
        result = 31 * result + maxResults;
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("entity", entityDescriptor).
                append("returnType", returnType).
                append("searchRestriction", searchRestriction).
                append("startIndex", startIndex).
                append("maxResults", maxResults).
                toString();
    }
}

package com.atlassian.crowd.search.query.membership;

import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import static com.atlassian.crowd.search.Entity.GROUP;

public class MembershipQuery<T> implements Query<T>
{
    private final EntityDescriptor entityToReturn;
    private final EntityDescriptor entityToMatch;
    private final boolean findChildren;
    private final String entityNameToMatch;
    private final int startIndex;
    private final int maxResults;
    private Class<T> returnType;

    public MembershipQuery(final Class<T> returnType, final boolean findChildren, final EntityDescriptor entityToMatch, final String entityNameToMatch, final EntityDescriptor entityToReturn, final int startIndex, final int maxResults)
    {
        Validate.notNull(entityToMatch, "entityToMatch argument cannot be null");
        Validate.notNull(entityNameToMatch, "entityNameToMatch argument cannot be null");
        Validate.notNull(entityToReturn, "entityToReturn argument cannot be null");
        Validate.isTrue(maxResults == EntityQuery.ALL_RESULTS || maxResults > 0, "maxResults must be greater than 0 (unless set to EntityQuery.ALL_RESULTS)");
        Validate.isTrue(startIndex >= 0, "startIndex cannot be less than zero");
        Validate.notNull(returnType, "returnType cannot be null");

        if (findChildren)
        {
            Validate.isTrue(entityToMatch.getEntityType() == GROUP, "Cannot find the children of type: " + entityToMatch);
        }
        else
        {
            Validate.isTrue(entityToReturn.getEntityType() == GROUP, "Cannot return parents of type: " + entityToMatch);
        }

        this.entityToReturn = entityToReturn;
        this.entityToMatch = entityToMatch;
        this.findChildren = findChildren;
        this.entityNameToMatch = entityNameToMatch;
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.returnType = returnType;
    }

    public MembershipQuery(final MembershipQuery<T> query, final int startIndex, final int maxResults)
    {
        this(query.getReturnType(), query.isFindChildren(), query.getEntityToMatch(), query.getEntityNameToMatch(), query.getEntityToReturn(), startIndex, maxResults);
    }

    public MembershipQuery(final MembershipQuery query, final Class<T> returnType)
    {
        this(returnType, query.isFindChildren(), query.getEntityToMatch(), query.getEntityNameToMatch(), query.getEntityToReturn(), query.getStartIndex(), query.getMaxResults());
    }

    public EntityDescriptor getEntityToReturn()
    {
        return entityToReturn;
    }

    public EntityDescriptor getEntityToMatch()
    {
        return entityToMatch;
    }

    public boolean isFindChildren()
    {
        return findChildren;
    }

    public String getEntityNameToMatch()
    {
        return entityNameToMatch;
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

    public SearchRestriction getSearchRestriction()
    {
        return NullRestrictionImpl.INSTANCE;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MembershipQuery)) return false;

        MembershipQuery that = (MembershipQuery) o;

        if (findChildren != that.findChildren) return false;
        if (maxResults != that.maxResults) return false;
        if (startIndex != that.startIndex) return false;
        if (entityNameToMatch != null ? !entityNameToMatch.equals(that.entityNameToMatch) : that.entityNameToMatch != null) return false;
        if (entityToMatch != null ? !entityToMatch.equals(that.entityToMatch) : that.entityToMatch != null) return false;
        if (entityToReturn != null ? !entityToReturn.equals(that.entityToReturn) : that.entityToReturn != null) return false;
        if (returnType != that.returnType) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = entityToReturn != null ? entityToReturn.hashCode() : 0;
        result = 31 * result + (entityToMatch != null ? entityToMatch.hashCode() : 0);
        result = 31 * result + (findChildren ? 1 : 0);
        result = 31 * result + (entityNameToMatch != null ? entityNameToMatch.hashCode() : 0);
        result = 31 * result + startIndex;
        result = 31 * result + maxResults;
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("entityToReturn", entityToReturn).
                append("entityToMatch", entityToMatch).
                append("findChildren", findChildren).
                append("entityNameToMatch", entityNameToMatch).
                append("startIndex", startIndex).
                append("maxResults", maxResults).
                toString();
    }
}

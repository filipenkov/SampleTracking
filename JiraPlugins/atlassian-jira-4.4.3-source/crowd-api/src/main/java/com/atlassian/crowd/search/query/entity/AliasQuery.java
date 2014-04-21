package com.atlassian.crowd.search.query.entity;

import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.embedded.api.SearchRestriction;

public class AliasQuery extends EntityQuery<String>
{
    public AliasQuery(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
    {
        super(String.class, EntityDescriptor.alias(), searchRestriction, startIndex, maxResults);
    }
}

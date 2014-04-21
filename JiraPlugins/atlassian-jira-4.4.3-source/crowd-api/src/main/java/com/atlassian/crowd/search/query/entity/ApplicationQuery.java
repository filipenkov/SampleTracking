package com.atlassian.crowd.search.query.entity;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.model.application.Application;

public class ApplicationQuery extends EntityQuery<Application>
{
    public ApplicationQuery(SearchRestriction searchRestriction, int startIndex, int maxResults)
    {
        super(Application.class, EntityDescriptor.application(), searchRestriction, startIndex, maxResults);
    }
}

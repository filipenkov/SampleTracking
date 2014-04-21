package com.atlassian.crowd.search.query.entity;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.EntityDescriptor;

import static com.atlassian.crowd.search.query.QueryUtils.checkAssignableFrom;

public class GroupQuery<T> extends EntityQuery<T>
{
    public GroupQuery(final Class<T> returnType, final GroupType groupType, final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
    {
        super(checkAssignableFrom(returnType, String.class, com.atlassian.crowd.embedded.api.Group.class, com.atlassian.crowd.model.group.Group.class), EntityDescriptor.group(groupType), searchRestriction, startIndex, maxResults);
    }
}

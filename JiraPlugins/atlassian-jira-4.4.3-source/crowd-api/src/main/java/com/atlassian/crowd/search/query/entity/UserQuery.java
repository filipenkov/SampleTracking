package com.atlassian.crowd.search.query.entity;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;

import static com.atlassian.crowd.search.query.QueryUtils.checkAssignableFrom;

public class UserQuery<T> extends EntityQuery<T>
{
    public UserQuery(final Class<T> returnType, final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
    {
        // since embedded User isAssignableFrom model User, we don't have to add model User to the check parameters
        super(checkAssignableFrom(returnType, String.class, User.class), EntityDescriptor.user(), searchRestriction, startIndex, maxResults);
    }
}

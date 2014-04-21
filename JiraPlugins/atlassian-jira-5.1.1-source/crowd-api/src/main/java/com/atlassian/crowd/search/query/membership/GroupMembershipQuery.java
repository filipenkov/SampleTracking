package com.atlassian.crowd.search.query.membership;

import com.atlassian.crowd.search.EntityDescriptor;

import static com.atlassian.crowd.search.query.QueryUtils.checkAssignableFrom;

public class GroupMembershipQuery<T> extends MembershipQuery<T>
{
    public GroupMembershipQuery(Class<T> returnType, boolean findMembers, EntityDescriptor entityToMatch, String entityNameToMatch, EntityDescriptor entityToReturn, int startIndex, int maxResults)
    {
        super(checkAssignableFrom(returnType, String.class, com.atlassian.crowd.embedded.api.Group.class, com.atlassian.crowd.model.group.Group.class), findMembers, entityToMatch, entityNameToMatch, entityToReturn, startIndex, maxResults);
    }
}

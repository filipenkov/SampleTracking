package com.atlassian.crowd.search.query.membership;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import static com.atlassian.crowd.search.query.QueryUtils.checkAssignableFrom;

public class UserMembersOfGroupQuery<T> extends MembershipQuery<T>
{
    public UserMembersOfGroupQuery(Class<T> returnType, boolean findMembers, EntityDescriptor entityToMatch, String entityNameToMatch, EntityDescriptor entityToReturn, int startIndex, int maxResults)
    {
        super(checkAssignableFrom(returnType, String.class, User.class), findMembers, entityToMatch, entityNameToMatch, entityToReturn, startIndex, maxResults);
    }
}

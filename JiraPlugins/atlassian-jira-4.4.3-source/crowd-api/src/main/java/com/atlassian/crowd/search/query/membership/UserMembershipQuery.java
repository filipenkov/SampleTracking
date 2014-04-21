package com.atlassian.crowd.search.query.membership;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import static com.atlassian.crowd.search.query.QueryUtils.checkAssignableFrom;

public class UserMembershipQuery<T> extends MembershipQuery<T>
{
    public UserMembershipQuery(final Class<T> returnType, final boolean findMembers, final EntityDescriptor entityToMatch, final String entityNameToMatch, final EntityDescriptor entityToReturn, final int startIndex, final int maxResults)
    {
        super(checkAssignableFrom(returnType, String.class, User.class), findMembers, entityToMatch, entityNameToMatch, entityToReturn, startIndex, maxResults);
    }
}

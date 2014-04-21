package com.atlassian.crowd.search.query.entity;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.model.token.Token;

public class TokenQuery extends EntityQuery<Token>
{
    public TokenQuery(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
    {
        super(Token.class, EntityDescriptor.token(), searchRestriction, startIndex, maxResults);
    }
}

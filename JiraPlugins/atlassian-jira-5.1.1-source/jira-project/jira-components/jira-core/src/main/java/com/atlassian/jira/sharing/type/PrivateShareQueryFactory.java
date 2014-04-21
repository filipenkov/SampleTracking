/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * Special ShareQueryFactory for the implied Private share type.
 * 
 * @since v3.13
 */
public class PrivateShareQueryFactory implements ShareQueryFactory<PrivateShareTypeSearchParameter>
{
    private static final class Name
    {
        static final String FIELD = "owner";
    }

    public Field getField(final SharedEntity entity, final SharePermission permission)
    {
        Assertions.not("entity must be private", !entity.getPermissions().isPrivate());
        final String ownerUserName = entity.getOwnerUserName();
        return new Field(Name.FIELD, (ownerUserName == null) ? "" : toLowerCase(ownerUserName), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    public Term[] getTerms(final User user)
    {
        final Term term = getTerm(user);
        return (term == null) ? new Term[0] : new Term[] { term };
    }

    public Query getQuery(final ShareTypeSearchParameter parameter, final User user)
    {
        final Term term = getTerm(user);
        return (term == null) ? null : new TermQuery(term);
    }

    public Query getQuery(final ShareTypeSearchParameter parameter)
    {
        throw new UnsupportedOperationException("Can't query for Private Shares");
    }

    private Term getTerm(final User user)
    {
        return (user == null) ? null : new Term(Name.FIELD, toLowerCase(user.getName()));
    }
}

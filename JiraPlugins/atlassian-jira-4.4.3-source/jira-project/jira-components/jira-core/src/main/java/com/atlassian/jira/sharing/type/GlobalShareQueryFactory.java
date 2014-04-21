/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.GlobalShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Implementation of the {@link ShareQueryFactory} for the Global share type.
 * 
 * @since v3.13
 */
public class GlobalShareQueryFactory implements ShareQueryFactory<GlobalShareTypeSearchParameter>
{
    private static final class Constant
    {
        static final String FIELD = "shareTypeGlobal";
        static final String VALUE = "true";
    }

    private Term getTerm(final ShareTypeSearchParameter parameter, final User user)
    {
        return new Term(Constant.FIELD, Constant.VALUE);
    }

    public Term[] getTerms(final User user)
    {
        return new Term[] { getTerm(null, null) };
    }

    public Query getQuery(final ShareTypeSearchParameter parameter, final User user)
    {
        return getQuery(parameter);
    }

    public Query getQuery(final ShareTypeSearchParameter parameter)
    {
        return new TermQuery(getTerm(parameter, null));
    }

    public Field getField(final SharedEntity entity, final SharePermission permission)
    {
        return new Field(Constant.FIELD, Constant.VALUE, Field.Store.YES, Field.Index.UN_TOKENIZED);
    }
}

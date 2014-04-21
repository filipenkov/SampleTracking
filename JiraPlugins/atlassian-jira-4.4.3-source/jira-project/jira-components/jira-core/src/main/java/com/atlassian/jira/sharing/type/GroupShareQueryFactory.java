/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.GroupShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since v3.13
 */
public class GroupShareQueryFactory implements ShareQueryFactory<GroupShareTypeSearchParameter>
{
    private final GroupManager groupManager;

    private static final class Name
    {
        static final String FIELD = "shareTypeGroup";
    }

    public GroupShareQueryFactory(GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    public Query getQuery(final ShareTypeSearchParameter searchParameter, final User user)
    {
        return getQuery(searchParameter);
    }

    public Query getQuery(final ShareTypeSearchParameter searchParameter)
    {
        return new TermQuery(new Term(Name.FIELD, ((GroupShareTypeSearchParameter) searchParameter).getGroupName()));
    }

    public Term[] getTerms(final User user)
    {
        if (user == null)
        {
            return new Term[0];
        }
        // search for all visible group shares
        final Iterable<String> groups = groupManager.getGroupNamesForUser(user.getName());
        final Collection<Term> result = new ArrayList<Term>();
        for (final String element : groups)
        {
            result.add(new Term(Name.FIELD, element));
        }
        return result.toArray(new Term[result.size()]);
    }

    public Field getField(final SharedEntity entity, final SharePermission permission)
    {
        return new Field(Name.FIELD, new GroupSharePermission(permission).getGroupName(), Field.Store.YES, Field.Index.NOT_ANALYZED);
    }
}

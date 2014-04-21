/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.type.GlobalShareQueryFactory;
import com.atlassian.jira.sharing.type.GroupShareQueryFactory;
import com.atlassian.jira.sharing.type.PrivateShareQueryFactory;
import com.atlassian.jira.sharing.type.ProjectShareQueryFactory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

/**
 * Create a permission query.
 * 
 * @since v3.13
 */
public class PermissionQueryFactory implements QueryFactory
{
    private final PrivateShareQueryFactory privateShareQueryFactory = new PrivateShareQueryFactory();
    private final GlobalShareQueryFactory globalShareQueryFactory = new GlobalShareQueryFactory();
    private final ProjectShareQueryFactory projectShareQueryFactory;
    private final GroupShareQueryFactory groupShareQueryFactory;

    public PermissionQueryFactory(final ProjectShareQueryFactory projectShareQueryFactory, final GroupManager groupManager)
    {
        this.projectShareQueryFactory = projectShareQueryFactory;
        this.groupShareQueryFactory = new GroupShareQueryFactory(groupManager);
    }

    public Query create(final SharedEntitySearchParameters searchParameters, final User user)
    {
        final QueryBuilder builder = new QueryBuilder();
        builder.add(privateShareQueryFactory.getTerms(user), BooleanClause.Occur.SHOULD);
        builder.add(globalShareQueryFactory.getTerms(user), BooleanClause.Occur.SHOULD);
        builder.add(groupShareQueryFactory.getTerms(user), BooleanClause.Occur.SHOULD);
        builder.add(projectShareQueryFactory.getTerms(user), BooleanClause.Occur.SHOULD);
        return builder.toQuery();
    }

    @Override
    public Query create(SharedEntitySearchParameters searchParameters, com.opensymphony.user.User user)
    {
        return create(searchParameters, (User) user);
    }

    /**
     * specifically unsupported here as this is designed for non-permission queries.
     */
    public Query create(final SharedEntitySearchParameters searchParameters)
    {
        throw new UnsupportedOperationException();
    }
}

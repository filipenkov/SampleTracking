package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.DescriptionClauseQueryFactory;
import com.atlassian.jira.jql.validator.DescriptionValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Class to create the {@link com.atlassian.jira.issue.search.SearchHandler} for the description field.
 *
 * @since v4.0
 */
@InjectableComponent
public final class DescriptionSearchHandlerFactory extends SimpleSearchHandlerFactory
{
    public DescriptionSearchHandlerFactory(final ComponentFactory componentFactory, DescriptionClauseQueryFactory queryFactory,
            DescriptionValidator queryValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory)
    {
        super(componentFactory, SystemSearchConstants.forDescription(),
                QuerySearcher.class, queryFactory, queryValidator, clausePermissionFactory,
                new SimpleClauseContextFactory(), null);
    }
}

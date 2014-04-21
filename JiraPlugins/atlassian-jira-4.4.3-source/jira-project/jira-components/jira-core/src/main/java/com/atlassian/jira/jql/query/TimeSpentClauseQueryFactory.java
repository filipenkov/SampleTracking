package com.atlassian.jira.jql.query;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDurationSupport;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Factory for producing clauses for the {@link com.atlassian.jira.issue.fields.TimeSpentSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public class TimeSpentClauseQueryFactory extends AbstractTimeTrackingClauseQueryFactory implements ClauseQueryFactory
{
    //CLOVER:OFF
    public TimeSpentClauseQueryFactory(final JqlOperandResolver operandResolver, final JqlDurationSupport jqlDurationSupport, final ApplicationProperties applicationProperties)
    {
        super(SystemSearchConstants.forTimeSpent().getIndexField(), operandResolver, jqlDurationSupport, applicationProperties);
    }
    //CLOVER:ON
}
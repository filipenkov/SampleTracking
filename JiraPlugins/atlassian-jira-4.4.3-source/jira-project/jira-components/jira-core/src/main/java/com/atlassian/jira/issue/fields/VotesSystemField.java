package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.IssueLongFieldComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.VotesStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: owenfellows
 * Date: 05-Aug-2004
 * Time: 09:24:27
 * To change this template use File | Settings | File Templates.
 */
public class VotesSystemField extends NavigableFieldImpl
{
    public VotesSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        super(IssueFieldConstants.VOTES, "issue.field.vote", "issue.column.heading.vote", NavigableField.ORDER_DESCENDING, new IssueLongFieldComparator(IssueFieldConstants.VOTES), velocityManager, applicationProperties, authenticationContext);
    }

    public LuceneFieldSorter getSorter()
    {
        return VotesStatisticsMapper.MAPPER;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put(getId(), issue.getVotes());
        return renderTemplate("votes-columnview.vm", velocityParams);
    }
}

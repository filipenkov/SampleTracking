package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.IssueLongFieldComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.WatchesStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

/**
 * @since 4.4
 */
public class WatchesSystemField extends NavigableFieldImpl
{
    public WatchesSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        super(IssueFieldConstants.WATCHES, "issue.field.watch", "issue.column.heading.watch", NavigableField.ORDER_DESCENDING, new IssueLongFieldComparator(IssueFieldConstants.WATCHES), velocityManager, applicationProperties, authenticationContext);
    }

    public LuceneFieldSorter getSorter()
    {
        return WatchesStatisticsMapper.MAPPER;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put(getId(), issue.getWatches());
        return renderTemplate("watches-columnview.vm", velocityParams);
    }
}

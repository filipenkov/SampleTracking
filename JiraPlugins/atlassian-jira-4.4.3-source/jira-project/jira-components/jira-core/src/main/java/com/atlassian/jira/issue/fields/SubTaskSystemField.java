package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.SubTaskFieldComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.SubTaskStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

public class SubTaskSystemField extends NavigableFieldImpl
{
    private final SubTaskManager subTaskManager;
    private final SubTaskStatisticsMapper subTaskStatisticsMapper;

    public SubTaskSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
                              SubTaskManager subTaskManager, SubTaskStatisticsMapper subTaskStatisticsMapper)
    {
        super(IssueFieldConstants.SUBTASKS, "issue.field.subtasks", "issue.column.heading.subtasks", NavigableField.ORDER_ASCENDING, new SubTaskFieldComparator(),
                velocityManager, applicationProperties, authenticationContext);
        this.subTaskManager = subTaskManager;
        this.subTaskStatisticsMapper = subTaskStatisticsMapper;
    }

    public LuceneFieldSorter getSorter()
    {
        return subTaskStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("subtasks", issue.getSubTaskObjects());
        velocityParams.put("applicationProperties", getApplicationProperties());
        return renderTemplate("subtask-columnview.vm", velocityParams);
    }
}

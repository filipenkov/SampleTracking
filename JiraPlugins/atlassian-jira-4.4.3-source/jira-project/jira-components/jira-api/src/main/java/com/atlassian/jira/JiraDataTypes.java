package com.atlassian.jira;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.option.CascadingOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.ICommentSystemField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.types.Duration;
import com.atlassian.jira.util.collect.MapBuilder;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Defines the known {@link com.atlassian.jira.JiraDataType data types}.
 *
 * @since v4.0
 */
public final class JiraDataTypes
{
    /**
     * Defines the core JIRA data types
     */
    public static final JiraDataType ISSUE = new JiraDataTypeImpl(Issue.class);
    public static final JiraDataType PROJECT = new JiraDataTypeImpl(Project.class);
    public static final JiraDataType PROJECT_CATEGORY = new JiraDataTypeImpl(ProjectCategory.class);
    public static final JiraDataType VERSION = new JiraDataTypeImpl(Version.class);
    public static final JiraDataType COMPONENT = new JiraDataTypeImpl(ProjectComponent.class);
    public static final JiraDataType USER = new JiraDataTypeImpl(User.class);
    public static final JiraDataType GROUP = new JiraDataTypeImpl(Group.class);
    public static final JiraDataType PROJECT_ROLE = new JiraDataTypeImpl(ProjectRole.class);
    public static final JiraDataType PRIORITY = new JiraDataTypeImpl(Priority.class);
    public static final JiraDataType RESOLUTION = new JiraDataTypeImpl(Resolution.class);
    public static final JiraDataType ISSUE_TYPE = new JiraDataTypeImpl(IssueType.class);
    public static final JiraDataType STATUS = new JiraDataTypeImpl(Status.class);
    public static final JiraDataType CASCADING_OPTION = new JiraDataTypeImpl(CascadingOption.class);
    public static final JiraDataType OPTION = new JiraDataTypeImpl(Option.class);
    public static final JiraDataType SAVED_FILTER = new JiraDataTypeImpl(SearchRequest.class);
    public static final JiraDataType ISSUE_SECURITY_LEVEL = new JiraDataTypeImpl(IssueSecurityLevel.class);
    public static final JiraDataType LABEL = new JiraDataTypeImpl(Label.class);

    public static final JiraDataType DATE = new JiraDataTypeImpl(Date.class);
    public static final JiraDataType TEXT = new JiraDataTypeImpl(String.class);
    public static final JiraDataType NUMBER = new JiraDataTypeImpl(Number.class);
    public static final JiraDataType DURATION = new JiraDataTypeImpl(Duration.class);
    public static final JiraDataType URL = new JiraDataTypeImpl(java.net.URL.class);

    public static final JiraDataType ALL = new JiraDataTypeImpl(Object.class);

    public static String getType(final Field field)
    {
        if (field instanceof ICommentSystemField)
        {
            return field.getClass().getCanonicalName();
        }

        final JiraDataType dataType = getFieldType(field.getId());
        if (dataType == null)
        {
            return field.getClass().getCanonicalName();
        }

        final Collection<String> stringCollection = dataType.asStrings();
        if (stringCollection.size() == 1)
        {
            return stringCollection.iterator().next();
        }
        else
        {
            return stringCollection.toString();
        }
    }

    // This is primarily for generating REST documentation and other such things where you
    // can't get a Field easily. In real production code you should probably be using the other version
    public static String getType(final String fieldId)
    {
        final JiraDataType dataType = getFieldType(fieldId);
        if (dataType == null)
        {
            return fieldId;
        }
        final Collection<String> stringCollection = dataType.asStrings();
        if (stringCollection.size() == 1)
        {
            return stringCollection.iterator().next();
        }
        else
        {
            return stringCollection.toString();
        }
    }

    public static JiraDataType getFieldType(final String fieldId)
    {
        final Map<String,JiraDataType> map = MapBuilder.<String, JiraDataType>newBuilder()
                .add(IssueFieldConstants.PROJECT, JiraDataTypes.PROJECT)
                .add(IssueFieldConstants.AFFECTED_VERSIONS, JiraDataTypes.VERSION)
                .add(IssueFieldConstants.ASSIGNEE, JiraDataTypes.USER)
                .add(IssueFieldConstants.COMPONENTS, JiraDataTypes.COMPONENT)
                .add(IssueFieldConstants.COMMENT, JiraDataTypes.TEXT)
                .add(IssueFieldConstants.DESCRIPTION, JiraDataTypes.TEXT)
                .add(IssueFieldConstants.DUE_DATE, JiraDataTypes.DATE)
                .add(IssueFieldConstants.ENVIRONMENT, JiraDataTypes.TEXT)
                .add(IssueFieldConstants.FIX_FOR_VERSIONS, JiraDataTypes.VERSION)
                .add(IssueFieldConstants.ISSUE_KEY, JiraDataTypes.ISSUE)
                .add(IssueFieldConstants.ISSUE_TYPE, JiraDataTypes.ISSUE_TYPE)
                .add(IssueFieldConstants.PRIORITY, JiraDataTypes.PRIORITY)
                .add(IssueFieldConstants.REPORTER, JiraDataTypes.USER)
                .add(IssueFieldConstants.SECURITY, JiraDataTypes.ISSUE_SECURITY_LEVEL)
                .add(IssueFieldConstants.SUMMARY, JiraDataTypes.TEXT)
                .add(IssueFieldConstants.CREATED, JiraDataTypes.DATE)
                .add(IssueFieldConstants.UPDATED, JiraDataTypes.DATE)
                .add(IssueFieldConstants.RESOLUTION_DATE, JiraDataTypes.DATE)
                .add(IssueFieldConstants.STATUS, JiraDataTypes.STATUS)
                .add(IssueFieldConstants.RESOLUTION, JiraDataTypes.RESOLUTION)
                .add(IssueFieldConstants.LABELS, JiraDataTypes.LABEL)
                .toFastMap();

        return map.get(fieldId);

        // Jira Data Types not present:
        // PROJECT_CATEGORY
        // GROUP
        // PROJECT_ROLE
        // CASCADING_OPTION
        // OPTION
        // SAVED_FILTER
        // NUMBER
        // DURATION
        // URL

        // Issue Field Constants not handled:
        // THUMBNAIL
        // ISSUE_LINKS (issue?)
        // WORKRATIO
        // SUBTASKS (issue?)
        // ATTACHMENT
        // TIMETRACKING
        // WORKLOG
        // TIME_ORIGINAL_ESTIMATE
        // TIME_ESTIMATE
        // TIME_SPENT
        // AGGREGATE_TIME_SPENT
        // AGGREGATE_TIME_ESTIMATE
        // AGGREGATE_TIME_ORIGINAL_ESTIMATE
        // AGGREGATE_PROGRESS
        // PROGRESS
        // VOTES
        // VOTERS
        // WATCHERS
    }

}

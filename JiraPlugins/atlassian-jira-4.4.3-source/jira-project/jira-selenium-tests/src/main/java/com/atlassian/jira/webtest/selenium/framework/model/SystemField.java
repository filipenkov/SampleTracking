package com.atlassian.jira.webtest.selenium.framework.model;

import com.atlassian.jira.webtest.framework.core.component.Option;
import com.atlassian.jira.webtest.framework.core.component.Options;

/**
 * Enumeration of JIRA system fields.
 *
 * @since v4.2
 */
public enum SystemField
{
    PROJECT("project", "Project"),
    AFFECTED_VERSIONS("versions", "Affects Versions"),
    ASSIGNEE("assignee", "Assignee"),
    COMPONENTS("components", "Components"),
    COMMENT("comment", "Comments"),
    DESCRIPTION("description", "Description"),
    DUE_DATE("duedate", "Due Date"),
    ENVIRONMENT("environment", "Environment"),
    FIX_VERSIONS("fixVersions", "Fix For Versions"),
    ISSUE_TYPE("issuetype", "Issue Type"),
    PRIORITY("priority", "Priority"),
    REPORTER("reporter", "Reporter"),
    SECURITY("security", "Issue Level Security"),
    SUMMARY("summary", "Summary"),
    TIMETRACKING("timetracking", "Time Tracking"),
    CREATED("created", "Created date"),
    UPDATED("updated", "Updated date"),
    RESOLUTION_DATE("resolutiondate", "Resolution Date"),
    STATUS("status", "Status"),
    RESOLUTION("resolution", "Resolution"),
    LABELS("labels", "Labels"),
    LOG_WORK("worklog", "Work Log");

    private final String id;
    private final String label;
    private final Option option;

    SystemField(String id, String label)
    {
        this.id = id;
        this.label = label;
        // field ID is an option value
        this.option = Options.full(null, id, label);
    }

    public String id()
    {
        return id;
    }

    public String label()
    {
        return label;
    }

    public Option option()
    {
        return option;
    }
}

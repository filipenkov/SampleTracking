package com.atlassian.jira.webtest.framework.model.admin;

/**
 * Enumeration of JIRA system fields. System fields are predefined JIRA fields and come with a human-readable ID.
 *
 * @see AdminField
 * @since v4.2
 */
public enum SystemAdminField implements AdminField
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

    SystemAdminField(String id, String label)
    {
        this.id = id;
        this.label = label;
    }

    public String id()
    {
        return this.id;
    }

    public String fieldName()
    {
        return label;
    }
}

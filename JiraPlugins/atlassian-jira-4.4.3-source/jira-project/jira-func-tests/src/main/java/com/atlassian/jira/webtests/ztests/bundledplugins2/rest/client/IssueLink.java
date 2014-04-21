package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

/**
 * Representation of an issue link in the JIRA REST API.
 *
 * @since v4.3
 */
public class IssueLink
{
    public String issueKey;
    public String issue;
    public Type type;

    public static class Type
    {
        public String name;
        public String direction;
        public String description;
    }
}

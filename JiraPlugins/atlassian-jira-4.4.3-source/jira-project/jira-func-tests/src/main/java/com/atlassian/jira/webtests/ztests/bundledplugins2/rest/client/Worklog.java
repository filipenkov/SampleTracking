package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

/**
 * Representation of a worklog in the JIRA REST API.
 *
 * @since v4.3
 */
public class Worklog
{
    public String self;
    public String issue;
    public User author;
    public User updateAuthor;
    public String comment;
    public String created;
    public String updated;
    public String started;
    public long minutesSpent;
}

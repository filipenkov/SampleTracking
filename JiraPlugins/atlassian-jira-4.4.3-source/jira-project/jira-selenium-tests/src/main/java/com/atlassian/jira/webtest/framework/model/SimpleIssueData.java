package com.atlassian.jira.webtest.framework.model;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Simple {@link com.atlassian.jira.webtest.framework.model.IssueData} implementation.
 *
 * @since v4.3
 */
public class SimpleIssueData implements IssueData
{
    private final long id;
    private final String key;

    public SimpleIssueData(long id, String key)
    {
        this.id = greaterThan("id", id, 0);
        this.key = notNull("key", key);
    }

    @Override
    public long id()
    {
        return id;
    }

    @Override
    public String key()
    {
        return key;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || !IssueData.class.isInstance(other))
        {
            return false;
        }
        IssueData that = (SimpleIssueData) other;
        return this.id() == that.id() && this.key().equals(that.key());
    }

    @Override
    public int hashCode()
    {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return asString("SimpleIssueData[id=",id,",key=",key,"]");
    }
}

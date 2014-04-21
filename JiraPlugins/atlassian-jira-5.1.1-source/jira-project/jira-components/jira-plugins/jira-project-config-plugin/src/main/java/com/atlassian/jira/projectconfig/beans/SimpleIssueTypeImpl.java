package com.atlassian.jira.projectconfig.beans;

import com.atlassian.jira.issue.issuetype.IssueType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Representation of a simple issue type.
 *
 * @since v4.4
 */
public class SimpleIssueTypeImpl implements SimpleIssueType
{
    private final String iconUrl;
    private final String name;
    private final String description;
    private final String id;
    private final boolean subTask;
    private final boolean defaultIssueType;
    private final IssueType issueType;

    public SimpleIssueTypeImpl(final IssueType issueType, boolean defaultIssueType)
    {
        this.issueType = issueType;
        this.id = issueType.getId();
        this.name = issueType.getNameTranslation();
        this.description = issueType.getDescTranslation();
        this.iconUrl = issueType.getIconUrl();
        this.subTask = issueType.isSubTask();
        this.defaultIssueType = defaultIssueType;
    }

    public SimpleIssueTypeImpl(final String id, final String name, final String description, final String iconUrl, final boolean subTask,
            final boolean defaultIssueType)
    {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.subTask = subTask;
        this.defaultIssueType = defaultIssueType;
        issueType = null;
        this.description = description;
    }

    @Override
    public String getIconUrl()
    {
        return iconUrl;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isDefault()
    {
        return isDefaultIssueType();
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public boolean isSubTask()
    {
        return subTask;
    }

    @Override
    public boolean isDefaultIssueType()
    {
        return defaultIssueType;
    }

    @Override
    public IssueType getConstant()
    {
        return issueType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SimpleIssueTypeImpl that = (SimpleIssueTypeImpl) o;

        if (defaultIssueType != that.defaultIssueType) { return false; }
        if (subTask != that.subTask) { return false; }
        if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = iconUrl != null ? iconUrl.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (subTask ? 1 : 0);
        result = 31 * result + (defaultIssueType ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

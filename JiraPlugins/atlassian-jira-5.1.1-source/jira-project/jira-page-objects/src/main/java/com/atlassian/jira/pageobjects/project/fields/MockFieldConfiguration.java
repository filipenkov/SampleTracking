package com.atlassian.jira.pageobjects.project.fields;

import com.atlassian.jira.pageobjects.pages.admin.EditFieldConfigPage;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;
import java.util.Map;

/**
 * Mock implementation for easy comparisons in tests.
 *
 * @since v4.4
 */
public class MockFieldConfiguration implements FieldConfiguration
{
    private String name;
    private List<Field> fields;
    private boolean isDefault;
    private List<IssueType> issueTypes = Lists.newArrayList();
    private boolean sharedProjects;
    private String sharedProjectsText;
    private boolean editLink;

    public MockFieldConfiguration(final String name)
    {
        this.name = name;
    }

    public MockFieldConfiguration fields(final List<Field> fields)
    {
        this.fields = fields;
        return this;
    }

    public MockFieldConfiguration setIsDefault(final boolean isDefault)
    {
        this.isDefault = isDefault;
        return this;
    }

    public MockFieldConfiguration setHasSharedProjects(final boolean sharedProjects)
    {
        this.sharedProjects = sharedProjects;
        return this;
    }

    public MockFieldConfiguration setSharedProjectText(final String sharedProjectsText)
    {
        this.sharedProjectsText = sharedProjectsText;
        return this;
    }

    public MockFieldConfiguration issueTypes(final Map<String, String> issueTypeToIcons)
    {
        for (final Map.Entry<String, String> issueTypeIcons: issueTypeToIcons.entrySet())
        {
            issueTypes.add(new MockIssueType(issueTypeIcons.getKey(), issueTypeIcons.getValue()));
        }
        return this;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public List<Field> getFields()
    {
        return fields;
    }

    @Override
    public boolean isDefault()
    {
        return isDefault;
    }

    @Override
    public List<IssueType> getIssueTypes()
    {
        return issueTypes;
    }

    @Override
    public SharedProjectsDialog openSharedProjects()
    {
        throw new UnsupportedOperationException("Not used for comparisons");
    }

    @Override
    public EditFieldConfigPage gotoEditFieldConfigPage()
    {
        throw new UnsupportedOperationException("Not used for comparisons");
    }

    @Override
    public boolean hasSharedProjects()
    {
        return sharedProjects;
    }

    @Override
    public String getSharedProjectsText()
    {
        return sharedProjectsText;
    }

    @Override
    public boolean hasEditLink()
    {
        return editLink;
    }

    public MockFieldConfiguration setEditLink(boolean editLink)
    {
        this.editLink = editLink;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof  FieldConfiguration))
        {
            return false;
        }
        FieldConfiguration rhs = (FieldConfiguration)o;
        return new EqualsBuilder()
                .append(name, rhs.getName())
                .append(fields, rhs.getFields())
                .append(isDefault, rhs.isDefault())
                .append(issueTypes, rhs.getIssueTypes())
                .append(sharedProjects, rhs.hasSharedProjects())
                .append(sharedProjectsText, rhs.getSharedProjectsText())
                .append(editLink, rhs.hasEditLink())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(name)
                .append(fields)
                .append(isDefault)
                .append(issueTypes)
                .append(sharedProjects)
                .append(sharedProjectsText)
                .append(editLink)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("name", name).
                append("fields", fields).
                append("isDefault", isDefault).
                append("issueTypes", issueTypes).
                append("sharedProjects", sharedProjects).
                append("sharedProjectsText", sharedProjectsText).
                append("editLink", editLink).
                toString();
    }

    public static class MockIssueType implements IssueType
    {

        private String name;
        private String iconSrc;

        public MockIssueType(final String name, final String iconSrc)
        {
            this.name = name;
            this.iconSrc = iconSrc;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getIconSrc()
        {
            return iconSrc;
        }

        @Override
        public boolean equals(Object o)
        {
            if(o == null || !(o instanceof IssueType))
            {
                return false;
            }
            final IssueType rhs = (IssueType)o;
            return new EqualsBuilder()
                    .append(name, rhs.getName())
                    .append(iconSrc, rhs.getIconSrc())
                    .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(name)
                    .append(iconSrc)
                    .toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                    append("name", name).
                    append("iconSrc", iconSrc).
                    toString();
        }
    }
}

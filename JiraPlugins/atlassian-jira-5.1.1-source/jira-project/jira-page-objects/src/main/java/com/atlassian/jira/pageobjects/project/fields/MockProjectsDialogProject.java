package com.atlassian.jira.pageobjects.project.fields;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Mock implementation for easy comparisons in tests.
 *
 * @since v4.4
 */
public class MockProjectsDialogProject implements ProjectsDialogProject
{
    private final String name;
    private final String avatarSrc;

    public MockProjectsDialogProject(final String name, final String avatarSrc)
    {
        this.name = name;
        this.avatarSrc = avatarSrc;
    }

    public String getName()
    {
        return name;
    }

    public String getAvatarSrc()
    {
        return avatarSrc;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof ProjectsDialogProject))
        {
            return false;
        }
        final ProjectsDialogProject rhs = (ProjectsDialogProject) o;

        return new EqualsBuilder()
                .append(name, rhs.getName())
                .append(avatarSrc, rhs.getAvatarSrc())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(name)
                .append(avatarSrc)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("name", name).
                append("avatarSrc", avatarSrc).
                toString();
    }
}

package com.atlassian.jira.pageobjects.project.summary.components;

import com.atlassian.jira.pageobjects.global.User;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @since v4.4
 */
//Very rough version. Flesh out as needed please.
public class ProjectComponent
{
    private final String name;
    private final User lead;

    public ProjectComponent(String name, User lead)
    {
        this.name = name;
        this.lead = lead;
    }

    public String getName()
    {
        return name;
    }

    public User getLead()
    {
        return lead;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ProjectComponent that = (ProjectComponent) o;

        if (lead != null ? !lead.equals(that.lead) : that.lead != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (lead != null ? lead.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}

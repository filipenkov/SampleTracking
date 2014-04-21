package com.atlassian.jira.projectconfig.beans;

import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
* Simple Field Config Scheme for use in the Project Configuration Fields Summary panel and Fields Panel
*
* @since v4.4
*/
public class SimpleFieldConfigScheme
{
    private final Long id;
    private final String name;
    private final String changeUrl;
    private final String editUrl;
    private final String description;

    public SimpleFieldConfigScheme(final FieldConfigurationScheme fieldConfigurationScheme,
            final String changeUrl, final String editUrl)
    {
        this.id = fieldConfigurationScheme.getId();
        this.name = fieldConfigurationScheme.getName();
        this.description = fieldConfigurationScheme.getDescription();
        this.changeUrl = changeUrl;
        this.editUrl = editUrl;
    }

    public SimpleFieldConfigScheme(final Long id, final String name, final String description,
            final String changeUrl, final String editUrl)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.changeUrl = changeUrl;
        this.editUrl = editUrl;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getChangeUrl()
    {
        return changeUrl;
    }

    public String getEditUrl()
    {
        return editUrl;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SimpleFieldConfigScheme that = (SimpleFieldConfigScheme) o;

        if (changeUrl != null ? !changeUrl.equals(that.changeUrl) : that.changeUrl != null) { return false; }
        if (description != null ? !description.equals(that.description) : that.description != null)
        {
            return false;
        }
        if (editUrl != null ? !editUrl.equals(that.editUrl) : that.editUrl != null) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (changeUrl != null ? changeUrl.hashCode() : 0);
        result = 31 * result + (editUrl != null ? editUrl.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

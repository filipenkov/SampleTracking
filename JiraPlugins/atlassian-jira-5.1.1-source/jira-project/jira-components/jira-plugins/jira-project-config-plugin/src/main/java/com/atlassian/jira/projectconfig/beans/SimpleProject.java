package com.atlassian.jira.projectconfig.beans;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.project.Project;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericValue;

/**
 * Wrapper for a project, mainly because the project's description can contain html
 *
 * @since v4.4
 */
public class SimpleProject
{
    private final Long id;
    private final String key;
    private final String name;
    private final String url;
    private final GenericValue projectCategory;
    private final String description;
    private final Avatar avatar;

    public SimpleProject(final Project project)
    {
        this.key = project.getKey();
        this.id = project.getId();
        this.name = project.getName();
        this.url = project.getUrl();
        this.description = project.getDescription();
        this.projectCategory = project.getProjectCategory();
        this.avatar = project.getAvatar();
    }

    public Long getId()
    {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public String getAbbreviatedUrl()
    {
        return StringUtils.abbreviate(url, 60);

    }

    public GenericValue getProjectCategory()
    {
        return projectCategory;
    }

    public String getDescriptionHtml()
    {
        return description;
    }

    public Avatar getAvatar()
    {
        return avatar;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SimpleProject that = (SimpleProject) o;

        if (avatar != null ? !avatar.getId().equals(that.avatar.getId()) : that.avatar != null) { return false; }
        if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
        if (!id.equals(that.id)) { return false; }
        if (!key.equals(that.key)) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
        if (projectCategory != null ? !projectCategory.equals(that.projectCategory) : that.projectCategory != null)
        { return false; }
        if (url != null ? !url.equals(that.url) : that.url != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (projectCategory != null ? projectCategory.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.getId().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

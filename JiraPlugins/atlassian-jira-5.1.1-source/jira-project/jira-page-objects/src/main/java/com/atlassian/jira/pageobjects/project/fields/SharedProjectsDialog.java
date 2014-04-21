package com.atlassian.jira.pageobjects.project.fields;

import com.atlassian.jira.pageobjects.components.InlineDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

/**
 * Represents the "shared projects" inline dialog for a specific Field Configuration on the Project Configuration
 * Fields tab
 *
 * @since v4.4
 */
public class SharedProjectsDialog extends InlineDialog
{

    @Inject
    private PageBinder pageBinder;

    public SharedProjectsDialog(final PageElement trigger, final String contentsId)
    {
        super(trigger, contentsId);
    }

    @Override
    public SharedProjectsDialog open()
    {
        super.open();
        return this;
    }

    @Override
    public InlineDialog close()
    {
        super.close();
        return this;
    }

    public List<ProjectsDialogProject> getProjects()
    {
        final List<ProjectsDialogProject> projects = Lists.newArrayList();

        final PageElement dialogContents = getDialogContents();
        final List<PageElement> projectElements = dialogContents.findAll(By.className("shared-project-name"));
        for (final PageElement projectElement : projectElements)
        {
            projects.add(pageBinder.bind(ProjectsDialogProjectImpl.class, projectElement));
        }
        return projects;
    }


    public static class ProjectsDialogProjectImpl implements ProjectsDialogProject
    {
        private PageElement projectListItem;
        private String avatarSrc;
        private String name;

        @Init
        public void initialize()
        {
            this.name = projectListItem.getText();
            this.avatarSrc = projectListItem.find(By.className("shared-project-icon")).getAttribute("src");
        }

        public ProjectsDialogProjectImpl(final PageElement projectListItem)
        {
            this.projectListItem = projectListItem;
        }

        public String getAvatarSrc()
        {
            return avatarSrc;
        }

        public String getName()
        {
            return name;
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
                    .append(getName(), rhs.getName())
                    .append(getAvatarSrc(), rhs.getAvatarSrc())
                    .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(getName())
                    .append(getAvatarSrc())
                    .toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                    append("name", getName()).
                    append("avatarSrc", getAvatarSrc()).
                    toString();
        }

    }
}

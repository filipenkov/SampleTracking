package com.atlassian.jira.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.NotNull;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Defines a project in JIRA.
 */
public interface Project
{
    public Long getId();

    public String getName();

    public String getKey();

    public String getUrl();

    /**
     * Returns the Project Lead
     * @return the Project Lead
     * @deprecated Use {@link #getLeadUser()} instead. 
     */
    public com.opensymphony.user.User getLead();
    
    /**
     * Returns the Project Lead
     * @return the Project Lead
     */
    public User getLeadUser();

    public String getLeadUserName();

    public String getDescription();

    public Long getAssigneeType();

    public Long getCounter();

    /**
     * Returns the components for this Project.
     * @deprecated Use {@link #getProjectComponents()}. Since v4.1.
     * @return the components for this Project.
     * @see #getProjectComponents()
     */
    public Collection<GenericValue> getComponents();

    /**
     * Returns the components for this Project.
     * @return the components for this Project.
     */
    public Collection<ProjectComponent> getProjectComponents();

    public Collection<Version> getVersions();

    public Collection<IssueType> getIssueTypes();

    public GenericValue getProjectCategory();

    /**
     * @deprecated only use this if you need to utilize an older API method
     * @return the GenericValue backing this project object
     */
    public GenericValue getGenericValue();

    /**
     * Gives the currently-configured {@link com.atlassian.jira.avatar.Avatar} for this project.
     * @return the current Avatar, never null.
     */
    @NotNull
    public Avatar getAvatar();
}

package com.atlassian.jira.project;

import com.atlassian.annotations.PublicApi;
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
@PublicApi
public interface Project
{
    /**
     * @return the id of the project
     */
    public Long getId();

    /**
     * @return the name of the project.
     */
    public String getName();

    /**
     * @return the project key.
     */
    public String getKey();

    /**
     * @return the project URL
     */
    public String getUrl();

    /**
     * @return the Project Lead
     */
    public User getLead();
    
    /**
     * Returns the Project Lead
     * @return the Project Lead
     * @deprecated Use {@link #getLead()} instead.
     */
    public User getLeadUser();

    /**
     * @return the user name of the project lead
     */
    public String getLeadUserName();

    /**
     * @return the project description
     */
    public String getDescription();

    /**
     * Returns the default assignee for issues that get created in this project.
     * Returns {@link AssigneeTypes#PROJECT_LEAD} or {@link AssigneeTypes#UNASSIGNED}.
     * Can return NULL if the default assignee has not been set for this project and this means the PROJECT LEAD is the default assignee.
     *
     * TODO: Write upgrade task to ensure default assignee is always set.
     *
     * @return the default assignee. NB: Can return NULL
     */
    public Long getAssigneeType();

    /**
     * @deprecated (since 5.0) The counter is not something users of project information should concern themselves with.
     * This call delegates through to ProjectManager.getCurrentCounterForProject().
     * @return the last number used to generate an issue key. E.g. Counter = 2, issue key: HSP-2
     */
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

    /**
     * @return a Collection of {@link Version} for this project
     */
    public Collection<Version> getVersions();

    /**
     * @return a Collection of {@link IssueType} for this project
     */
    public Collection<IssueType> getIssueTypes();

    /**
     * @return a GV containing the project category information for this project.
     *
     * @deprecated Use {@link #getProjectCategoryObject()} instead. Since v5.1.
     */
    public GenericValue getProjectCategory();

    /**
     * @return the project category information for this project.
     */
    public ProjectCategory getProjectCategoryObject();

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

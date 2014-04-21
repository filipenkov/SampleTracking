/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProjectLead extends AbstractProjectsSecurityType
{
    public static final String DESC = "lead";
    private JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectLead(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.project.lead");
    }

    public String getType()
    {
        return DESC;
    }

    public boolean hasPermission(GenericValue entity, String argument)
    {
        return false;
    }

    /**
     * Determines if the user is the project lead for the project. The current project is derived from the entity using JiraUtils.getProject.
     * If it is not then false is returned.
     *
     * @param entity        The Generic Value. Should be an Issue or a Project
     * @param argument      Not needed for this implementation
     * @param user          User to check the permission on. If it is null then the check is made on the current user
     * @param issueCreation
     * @return true if the user is the project lead otherwise false
     * @see com.atlassian.jira.security.type.CurrentAssignee#hasPermission
     * @see com.atlassian.jira.security.type.CurrentReporter#hasPermission
     * @see SingleUser#hasPermission
     * @see com.atlassian.jira.security.type.GroupDropdown#hasPermission
     */
    public boolean hasPermission(GenericValue entity, String argument, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
    {
        if (entity == null)
            throw new IllegalArgumentException("Entity passed must NOT be null");
        if (!("Project".equals(entity.getEntityName()) || "Issue".equals(entity.getEntityName())))
            throw new IllegalArgumentException("Entity passed must be a Project or an Issue not a " + entity.getEntityName());
        if (user == null)
            throw new IllegalArgumentException("User passed must not be null");

        String projectLead = null;
        if ("Project".equals(entity.getEntityName()))
        {
            projectLead = entity.getString("lead");
        }
        else if ("Issue".equals(entity.getEntityName()))
        {
            GenericValue project = ManagerFactory.getProjectManager().getProject(entity);
            projectLead = project.getString("lead");
        }

        //if there is a project lead user is the project lead then return true
        if (projectLead != null)
        {
            if (projectLead.equals(user.getName()))
                return true;
        }

        return false;
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    public Set<User> getUsers(PermissionContext ctx, String ignored)
    {
        GenericValue project = ctx.getProject();
        String username = project.getString("lead");
        Set result =  new HashSet(1);
        User user = UserUtils.getUser(username);
        if (user != null) result.add(user);
        return result;
    }

    @Override
    public Query getQuery(User searcher, Project project, GenericValue securityLevel, String parameter)
    {
        //JRA-21648 : Project Lead should not return query for issues that you have no permission for
        if (project.getLead().equals(searcher)) {
            final BooleanQuery query = new BooleanQuery();
            query.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID,""+project.getId())), BooleanClause.Occur.MUST);
            query.add(super.getQuery(searcher, securityLevel, parameter),BooleanClause.Occur.MUST);
            return query;
        }
        return null;
    }
}

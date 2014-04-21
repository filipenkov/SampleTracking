/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.project;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

@Deprecated
public class ProjectUtils
{
    public static GenericValue createProject(final Map<String, Object> fields)
            throws CreateException, GenericEntityException
    {
        if (fields.containsKey("id"))
        {
            throw new CreateException("Field 'id' was present, but is not allowed in create project");
        }
        if (!fields.containsKey("key") || !(fields.get("key") instanceof String))
        {
            throw new CreateException("Field 'key' was not present or not of type String: '" + fields.get("key") + "'");
        }
        if (!(fields.containsKey("lead") && ((fields.get("lead") instanceof String) || (fields.get("lead") instanceof User))))
        {
            throw new CreateException("Field 'lead' was not present or not of type String or User: '" + fields.get("lead") + "'");
        }
        if (!fields.containsKey("name") || !(fields.get("name") instanceof String))
        {
            throw new CreateException("Field 'name' was not present or not of type String: '" + fields.get("name") + "'");
        }

        if (!fields.containsKey("counter") || !(fields.get("counter") instanceof Long))
        {
            fields.put("counter", 0L);
        }
        if ((fields.get("lead") instanceof User))
        {
            fields.put("lead", ((User) fields.get("lead")).getName());
        }

        final ProjectService projectService = ComponentManager.getComponentInstanceOfType(ProjectService.class);
        final JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();

        final ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(authenticationContext.getUser(),
                (String) fields.get("name"), (String) fields.get("key"), (String) fields.get("description"), (String) fields.get("lead"),
                (String) fields.get("url"), null);

        if (!result.isValid())
        {
            throw new CreateException(result.getErrorCollection().toString());
        }

        final Project project = projectService.createProject(result);

        return project.getGenericValue();
    }
}

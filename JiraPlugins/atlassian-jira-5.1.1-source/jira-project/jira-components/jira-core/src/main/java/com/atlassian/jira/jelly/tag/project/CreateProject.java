/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessorImpl;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class CreateProject extends UserAwareActionTagSupport implements ProjectContextAccessor
{
    private Logger log = Logger.getLogger(CreateProject.class);
    private static final String KEY_PROJECTID = "pid";
    private static final String KEY_PROJECTKEY = "key";
    private static final String KEY_PROJECTNAME = "name";
    private static final String KEY_PROJECTLEAD = "lead";
    private static final String KEY_DEFAULTASSIGNEE = "assigneeType";
    private static final String KEY_PROJECTAVATAR = "avatarId";

    private final String[] requiredProperties;
    private final String[] requiredContextVariablesAfter = new String[] { JellyTagConstants.PROJECT_ID, JellyTagConstants.PROJECT_KEY };
    private final ProjectContextAccessor projectContextAccessor;

    public CreateProject()
    {
        setActionName("AddProject");
        projectContextAccessor = new ProjectContextAccessorImpl(this);
        requiredProperties = new String[] { KEY_PROJECTKEY, KEY_PROJECTNAME, KEY_PROJECTLEAD };
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        if (!getProperties().containsKey(KEY_DEFAULTASSIGNEE))
        {
            if (ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED))
                setProperty(KEY_DEFAULTASSIGNEE, String.valueOf(ProjectAssigneeTypes.UNASSIGNED));
            else
                setProperty(KEY_DEFAULTASSIGNEE, String.valueOf(ProjectAssigneeTypes.PROJECT_LEAD));

            //JRA-8920 - set the permission scheme to the default
            Long defaultSchemeId = ManagerFactory.getPermissionSchemeManager().getDefaultSchemeObject().getId();
            setProperty("permissionScheme", defaultSchemeId.toString());
        }
    }

    protected void postTagExecution(XMLOutput output) throws JellyTagException
    {
        log.debug("CreateProject.postTagExecution");
        copyRedirectUrlParametersToTag(getResponse().getRedirectUrl());
        setProject(getProperty("key"));
    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousProject();
    }

    public String[] getRequiredProperties()
    {
        return requiredProperties;
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return requiredContextVariablesAfter;
    }

    public void setProject(Long projectId)
    {
        projectContextAccessor.setProject(projectId);
    }

    public void setProject(String projectKey)
    {
        projectContextAccessor.setProject(projectKey);
    }

    public void setProject(GenericValue project)
    {
        projectContextAccessor.setProject(project);
    }

    public void loadPreviousProject()
    {
        projectContextAccessor.loadPreviousProject();
    }
}

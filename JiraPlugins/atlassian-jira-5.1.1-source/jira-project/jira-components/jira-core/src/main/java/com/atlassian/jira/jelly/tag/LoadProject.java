/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;

public class LoadProject extends JiraDynaBeanTagSupport
{
    private static final Logger log = Logger.getLogger(LoadManager.class);

    public LoadProject()
    {
        super();
    }

    public void doTag(XMLOutput xmlOutput) throws JellyTagException
    {
        try
        {
            String variableName = (String) getProperties().get("var");
            String projectName = (String) getProperties().get("project-name");
            String projectKey = (String) getProperties().get("project-key");
            if (projectName == null && projectKey == null)
                throw new JellyTagException("Please specify either project-name or project-key attribute for LoadProject tag");
            Collection projects = ManagerFactory.getProjectManager().getProjects();
            for (Iterator iterator = projects.iterator(); iterator.hasNext();)
            {
                GenericValue project = (GenericValue) iterator.next();
                if (project.getString("name").equals(projectName))
                {
                    getContext().setVariable(variableName, project);
                    return;
                }
                if (project.getString("key").equals(projectKey))
                {
                    getContext().setVariable(variableName, project);
                    return;
                }
            }
        }
        catch (Exception e)
        {
            log.error(e, e);
            throw new JellyTagException(e);
        }
    }
}

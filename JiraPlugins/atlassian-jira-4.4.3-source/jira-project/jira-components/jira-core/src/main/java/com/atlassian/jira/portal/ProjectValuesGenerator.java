/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.User;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * ValuesGenerator generating a Map of {project ID -> project name} pairs.
 */
public class ProjectValuesGenerator implements ValuesGenerator
{
    private static final Logger log = Logger.getLogger(ProjectValuesGenerator.class);

    public Map<String, String> getValues(final Map params)
    {
        final User u = (User) params.get("User"); // BUG this user is wrong - the params are being cached somewhere.

        try
        {
            final Collection<GenericValue> projectGVs = ManagerFactory.getPermissionManager().getProjects(Permissions.BROWSE, u);

            final Map<String, String> projects = ListOrderedMap.decorate(new HashMap<Long, String>(projectGVs.size()));
            for (final Object element : projectGVs)
            {
                final GenericValue project = (GenericValue) element;
                projects.put(project.getLong("id").toString(), project.getString("name"));
            }

            return projects;
        }
        catch (final Exception e)
        {
            log.error("Could not retrieve project values for this user: " + u.getName(), e);
            return null;
        }
    }
}

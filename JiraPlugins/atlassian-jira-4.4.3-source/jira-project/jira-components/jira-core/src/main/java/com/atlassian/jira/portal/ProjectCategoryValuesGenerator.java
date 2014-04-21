/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.user.User;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class ProjectCategoryValuesGenerator implements ValuesGenerator
{
    private static final Logger log = Logger.getLogger(ProjectCategoryValuesGenerator.class);

    public Map getValues(Map params)
    {
        User u = (User) params.get("User");
        Map result = new ListOrderedMap();
        I18nBean i18nBean = new I18nBean();
        result.put(null, i18nBean.getText("gadget.projects.display.name.all")); // make this backward compatible with existing databases

        try
        {
            for (Iterator i = ManagerFactory.getProjectManager().getProjectCategories().iterator(); i.hasNext();)
            {
                GenericValue pc = (GenericValue) i.next();
                Collection projects = ManagerFactory.getProjectManager().getProjectsFromProjectCategory(pc);
                int count = 0;

                for (Iterator j = projects.iterator(); j.hasNext();)
                {
                    GenericValue project = (GenericValue) j.next();
                    if (ManagerFactory.getPermissionManager().hasPermission(Permissions.BROWSE, project, u))
                    {
                        count++;
                    }
                }

                if (count > 0)
                {
                    result.put(pc.getLong("id").toString(), pc.getString("name"));
                }
            }
        }
        catch (Exception e)
        {
            log.error("Could not retrieve project values for this user: " + u.getName(), e);
            return null;
        }

        return result;
    }
}

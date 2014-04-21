/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractSchemeAwareAction;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeType;
import org.ofbiz.core.entity.GenericEntityException;

public class SchemeAwareIssueSecurityAction extends AbstractSchemeAwareAction
{
    public SchemeType getType(String id)
    {
        return ManagerFactory.getIssueSecurityTypeManager().getSchemeType(id);
    }

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getIssueSecuritySchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }

    /**
     * Is this Level the default level for the scheme
     * @param levelId The id of the level to check if it is the default
     * @return true if the level is the default otherwise false
     */
    public boolean isDefault(Long levelId)
    {
        if (levelId != null)
        {
            try
            {
                if (getScheme() != null)
                {
                    return levelId.equals(getScheme().getLong("defaultlevel"));
                }
            }
            catch (GenericEntityException e)
            {
            }
        }

        return false;
    }
}

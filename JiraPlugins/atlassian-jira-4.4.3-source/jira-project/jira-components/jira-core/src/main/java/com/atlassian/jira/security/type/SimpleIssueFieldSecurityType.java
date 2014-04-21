package com.atlassian.jira.security.type;

import com.atlassian.core.user.UserUtils;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

/**
 * The common class for IssueField SecurityTypes that rely on a simple field (ie a field of the Issue Generic Value).
 *
 * @since v4.3
 */
public abstract class SimpleIssueFieldSecurityType extends AbstractIssueFieldSecurityType
{

    protected abstract String getField();

    /**
     * Defines whether the given user has permission to see the given issue.
     *
     * @param user          the User for whom permission is being determined.
     * @param issueCreation not used.
     * @param issueGv       the issue.
     * @param argument      a parameter to be optionally used by overriders.
     * @return true only if the User has permission to see the issue, false if issueGv is not an issue.
     */
    @Override
    protected boolean hasIssuePermission(com.atlassian.crowd.embedded.api.User user, boolean issueCreation, GenericValue issueGv, String argument)
    {
        try
        {
            if (!"Issue".equals(issueGv.getEntityName()))
            {
                return false;
            }

            if (user == null)
            {
                user = (User) ActionContext.getPrincipal();
            }
            User issueUser = UserUtils.getUser(issueGv.getString(getField()));

            //if the issueUser is the current user then return true
            if (issueUser != null && issueUser.equals(user))
            {
                return true;
            }
        }
        catch (EntityNotFoundException e)
        {
            //Dont log anything
        }
        return false;
    }

}

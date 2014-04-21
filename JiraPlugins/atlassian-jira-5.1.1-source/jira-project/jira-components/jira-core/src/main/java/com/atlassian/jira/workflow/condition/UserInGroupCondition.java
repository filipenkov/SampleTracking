package com.atlassian.jira.workflow.condition;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Map;

/**
 * Workflow condition that checks if the caller is in the required argument "group".
 *
 * @since v5.0
 */
public class UserInGroupCondition extends AbstractJiraCondition
{
    @Override
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        User caller = getCaller(transientVars, args);
        if (caller == null)
        {
            return false;
        }

        String username = getCallerName(transientVars, args);
        String groupname = (String) args.get("group");
        return ComponentAccessor.getGroupManager().isUserInGroup(username, groupname);
    }
}

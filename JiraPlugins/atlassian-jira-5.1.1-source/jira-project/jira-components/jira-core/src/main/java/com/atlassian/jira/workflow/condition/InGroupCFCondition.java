package com.atlassian.jira.workflow.condition;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.util.GroupSelectorUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Returns true if the current user is in a group specified by a custom field.
 */
public class InGroupCFCondition extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(InGroupCFCondition.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        CustomFieldManager fieldManager = ComponentAccessor.getCustomFieldManager();
        User caller = getCaller(transientVars, args);
        Issue issue = getIssue(transientVars);

        String cfKey = (String) args.get("groupcf");
        String cfName = (String) args.get("groupcfname");
        if (cfKey == null && cfName == null)
        {
            log.warn("Workflow condition " + getClass() + " is not configured with a custom field id ('groupcf') or name ('groupcfname')");
            return false;
        }
        CustomField field = null;
        if (cfKey != null)
        {
            field = fieldManager.getCustomFieldObject(cfKey);
            if (field == null)
            {
                log.error("No custom field with key '" + cfKey + "'");
                return false;
            }
        }
        else
        {
            field = fieldManager.getCustomFieldObjectByName(cfName);
            if (field == null)
            {
                log.error("No custom field called '"+cfName+"'");
                return false;
            }
        }
        GroupSelectorUtils groupSelectorUtils = (GroupSelectorUtils) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(GroupSelectorUtils.class);
        return groupSelectorUtils.isUserInCustomFieldGroup(issue, field, caller);
    }
}

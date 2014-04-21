package com.atlassian.jira.workflow.function.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class AssignToLeadFunction extends AbstractJiraFunctionProvider
{
    private static final Logger log = Logger.getLogger(AssignToLeadFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        MutableIssue issue = getIssue(transientVars);
        String leadName = null;
        User lead = null;
        boolean componentLead = false;
        if (issue.getComponents() != null && issue.getComponents().size() > 0)
        {
            componentLead = true;

            GenericValue firstComponent = issue.getComponents().iterator().next();
            leadName = firstComponent.getString("lead");
        }
        if (leadName == null)
        {
            lead = issue.getProjectObject().getLead();
            leadName = (lead != null ? lead.getName() : null);
        }
        if (leadName == null)
        {
            return;
        }

        if (lead == null)
        {
            lead = getLead(leadName);

            if (lead == null)
            {
                log.error((componentLead ? "Component" : "Project") + " lead '" + leadName + "' in project " +
                        issue.getProjectObject().getName() + " does not exist");
                return;
            }
        }

        log.info("Automatically setting assignee to lead developer "+leadName);
        issue.setAssignee(lead);

        // JRA-14269: issue.store() should never have been called in this function, as it can cause the Issue object
        // to be persisted to the database prematurely. However, since it has been here for a while, removing it could
        // break existing functionality for lots of users. But, because an NPE is only thrown when this function is used
        // in the Create step, all we have to do to prevent this error from occuring is check if the issue has already
        // been stored before. If it has, we can call store() to update the issue, which maintains working (albeit
        // incorrect) behaviour. If it hasn't, we defer the store() call, as it should have been implemented initially.
        if (issue.isCreated())
        {
            issue.store();
        }
    }

    User getLead(String userName)
    {
        return UserUtils.getUser(userName);
    }
}

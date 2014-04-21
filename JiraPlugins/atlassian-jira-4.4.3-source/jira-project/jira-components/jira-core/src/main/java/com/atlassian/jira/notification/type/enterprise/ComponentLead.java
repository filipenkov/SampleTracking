package com.atlassian.jira.notification.type.enterprise;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.type.AbstractNotificationType;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ComponentLead extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(ComponentLead.class);
    private final JiraAuthenticationContext authenticationContext;

    public ComponentLead(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public String getDisplayName()
    {
        return authenticationContext.getI18nHelper().getText("admin.projects.component.lead");
    }

    public List getRecipients(IssueEvent event, String argument)
    {
        Issue issue = event.getIssue();
        if (issue != null)
        {
            Collection components = issue.getComponents();
            Set recipients = new HashSet();

            for (Iterator iterator = components.iterator(); iterator.hasNext();)
            {
                GenericValue component = (GenericValue) iterator.next();
                String userid = component.getString("lead");

                if(userid != null)
                {
                    try
                    {
                        User u = UserUtils.getUser(userid);
                        if (u != null)
                        {
                            recipients.add(new NotificationRecipient(u));
                        }
                        else
                        {
                            log.warn("Nonexistent user '" + userid + "' listed as component lead");
                        }
                    }
                    catch (EntityNotFoundException e)
                    {
                        log.warn("Unable to find component lead " + userid);
                    }
                }
            }

            return new ArrayList(recipients);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
}

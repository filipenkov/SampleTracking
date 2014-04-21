/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SingleUser extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(SingleUser.class);
    public static final String DESC = "Single_User";

    private JiraAuthenticationContext jiraAuthenticationContext;

    public SingleUser(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List getRecipients(IssueEvent event, String username)
    {
        User u = ManagerFactory.getUserManager().getUser(username);
        if (u != null)
        {
            return EasyList.build(new NotificationRecipient(u));
        }
        return Collections.EMPTY_LIST;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.single.user");
    }

    public String getType()
    {
        return "user";
    }

    public boolean doValidation(String key, Map parameters)
    {
        Object value = parameters.get(key);
        return (value != null && TextUtils.stringSet((String) value) && ManagerFactory.getUserManager().getUser((String) value) != null);
    }
}

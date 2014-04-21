/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GroupDropdown extends AbstractNotificationType
{
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public GroupDropdown(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List getRecipients(IssueEvent event, String groupName)
    {
        List recipients = new ArrayList();
        Collection users = getUserUtil().getUsersInGroupNames(EasyList.build(groupName));
        for (Iterator iterator = users.iterator(); iterator.hasNext();)
        {
            User user = (User) iterator.next();
            recipients.add(new NotificationRecipient(user));
        }
        return recipients;
    }

    /*
     * JIRA wont let me dependency inject this.  I would like to but it wont let me
     */
    private UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.group");
    }

    public String getType()
    {
        return "group";
    }

    public Collection getGroups()
    {
        return ComponentAccessor.getUserManager().getGroups();
    }

    public boolean doValidation(String key, Map parameters)
    {
        Object value = parameters.get(key);
        return (value != null && TextUtils.stringSet((String) value));
    }
}

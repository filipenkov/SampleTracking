/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.type.AbstractNotificationType;
import com.atlassian.core.util.collection.EasyList;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.Map;

public class TypeForTesting extends AbstractNotificationType
{
    public List getRecipients(IssueEvent event, String argument)
    {
        List recipients = EasyList.build(new NotificationRecipient(argument));

        return recipients;
    }

    public String getDisplayName()
    {
        return "Test Type";
    }

    public String getType()
    {
        return "test";
    }

    public boolean doValidation(String key, Map parameters)
    {
        Object value = parameters.get(key);
        return (value != null && TextUtils.stringSet((String) value));
    }
}

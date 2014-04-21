/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.ImportUtils;

import java.util.HashMap;
import java.util.Map;

public class UserEventDispatcher
{
    public static void dispatchEvent(int type, User user)
    {
        dispatchEvent(type, user, new HashMap());
    }

    public static void dispatchEvent(int type, User user, Map params)
    {
        params.put("baseurl", ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
        UserEvent event = new UserEvent(params, user, type);

        if (ImportUtils.isEnableNotifications())
        {
            ComponentManager.getComponentInstanceOfType(EventPublisher.class).publish(event);
        }
    }
}

/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

@WebSudoRequired
public class SchedulerAdmin extends JiraWebActionSupport
{
    private Scheduler scheduler;

    public Scheduler getScheduler() throws SchedulerException
    {
        if (scheduler == null)
            scheduler = ManagerFactory.getScheduler();

        return scheduler;
    }
}

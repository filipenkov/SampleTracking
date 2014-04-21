/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

@WebSudoRequired
public class MailQueueAdmin extends JiraWebActionSupport
{
    boolean flush = false;
    boolean resend = false;
    boolean delete = false;
    boolean unstick = false;
    private String page = "";

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        //ensure that we use a proper base url in e-mails that make use of the baseurl added to the velocity context
        //by the standard JiraVelocityUtils helper.
        DefaultVelocityRequestContextFactory.cacheVelocityRequestContext(getApplicationProperties().getString(APKeys.JIRA_BASEURL), null);
        if (flush)
        {
            ManagerFactory.getMailQueue().sendBuffer();
        }
        else if (resend)
        {
            ManagerFactory.getMailQueue().resendErrorQueue();
        }
        else if (delete)
        {
            ManagerFactory.getMailQueue().emptyErrorQueue();
        }
        else if (unstick)
        {
            ManagerFactory.getMailQueue().unstickQueue();
        }

        return getRedirect("MailQueueAdmin!default.jspa?page=" + getPage());
    }

    public MailQueue getMailQueue()
    {
        return ManagerFactory.getMailQueue();
    }

    public Collection<MailQueueItem> getQueuedItems()
    {
        // got to be careful here!
        Queue<MailQueueItem> queue;
        if ("errorqueue".equals(page))
            queue = getMailQueue().getErrorQueue();
        else
            queue = getMailQueue().getQueue();

        List<MailQueueItem> queueList = new ArrayList<MailQueueItem>();

        queueList.addAll(queue);

        return queueList;
    }

    public void setFlush(boolean flush)
    {
        this.flush = flush;
    }

    public void setResend(boolean resend)
    {
        this.resend = resend;
    }

    public void setDelete(boolean delete)
    {
        this.delete = delete;
    }

    public void setUnstick(boolean unstick)
    {
        this.unstick = unstick;
    }

    public String getPage()
    {
        return page;
    }

    public void setPage(String page)
    {
        this.page = page;
    }

    public boolean isSending()
    {
        return getMailQueue().isSending();
    }

    public String getPrettySendingStartTime()
    {
        OutlookDate outlookDate = ManagerFactory.getOutlookDateManager().getOutlookDate(getLocale());
        if (isSending())
        {
            return outlookDate.formatDMYHMS(getMailQueue().getSendingStarted());
        }
        else
        {
            return "";
        }
    }

    public String getTimeSpentSendingCurrentItem()
    {
        if (isSending())
        {
            Timestamp started = getMailQueue().getSendingStarted();
            long timeTaken = System.currentTimeMillis() - started.getTime();
            return DateUtils.getDurationString(timeTaken/1000);
        }
        else
        {
            return "";
        }
    }
    
    public boolean isMailSendingDisabled()
    {
        return MailFactory.isSendingDisabled();
    }

    public boolean isHasMailServer()
    {
        try
        {
            Object smtp = MailFactory.getServerManager().getDefaultSMTPMailServer();
            if (smtp != null) return true;
        }
        catch (Exception e)
        {
            // This isn't the place to die if anything is wrong
        }
        return false;
    }

    /** Whether any projects have associated notification schemes. */
    public boolean isEnabledNotificationSchemes()
    {
        Collection<GenericValue> projects = ManagerFactory.getProjectManager().getProjects();
        for (GenericValue project : projects)
        {
            try
            {
                List projectSchemes = ManagerFactory.getNotificationSchemeManager().getSchemes(project);
                if (projectSchemes.size() > 0)
                {
                    return true;
                }
            }
            catch (GenericEntityException e)
            {
                // This isn't the place to die if anything is wrong
            }
        }
        return false;
    }
}

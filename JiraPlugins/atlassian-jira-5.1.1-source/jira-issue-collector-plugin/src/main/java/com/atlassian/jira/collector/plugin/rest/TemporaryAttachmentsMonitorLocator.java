package com.atlassian.jira.collector.plugin.rest;

import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class TemporaryAttachmentsMonitorLocator
{
    public static TemporaryAttachmentsMonitor getAttachmentsMonitor(final HttpServletRequest request, final String collectorId)
    {
        final HttpSession session = request.getSession(true);
        if (session != null)
        {
            String sessionKey = "collector.temp.attachments." + collectorId;
            TemporaryAttachmentsMonitor monitor = (TemporaryAttachmentsMonitor) session.getAttribute(sessionKey);
            if (monitor == null)
            {
                monitor = new TemporaryAttachmentsMonitor();
                session.setAttribute(sessionKey, monitor);
            }
            return monitor;
        }
        return null;
    }
}

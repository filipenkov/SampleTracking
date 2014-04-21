package com.atlassian.streams.jira;

import com.atlassian.jira.util.thread.JiraThreadLocalUtils;
import com.atlassian.streams.spi.SessionManager;

import com.google.common.base.Supplier;

import org.apache.log4j.Logger;

public class JiraSessionManager implements SessionManager
{
    private static final Logger logger = Logger.getLogger(JiraSessionManager.class);

    public <T> T withSession(Supplier<T> s)
    {
        JiraThreadLocalUtils.preCall();
        try
        {
            return s.get();
        }
        finally
        {
            JiraThreadLocalUtils.postCall(logger, null);
        }
    }
}

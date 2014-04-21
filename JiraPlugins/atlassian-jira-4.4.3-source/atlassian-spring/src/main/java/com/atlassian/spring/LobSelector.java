package com.atlassian.spring;

import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Default implementation of the lob selector, returns the configured default LobHandler.
 */
public class LobSelector
{
    LobHandler defaultHandler;

    public LobHandler getLobHandler()
    {
        return defaultHandler;
    }

    public void setDefaultHandler(LobHandler defaultHandler)
    {
        this.defaultHandler = defaultHandler;
    }
}

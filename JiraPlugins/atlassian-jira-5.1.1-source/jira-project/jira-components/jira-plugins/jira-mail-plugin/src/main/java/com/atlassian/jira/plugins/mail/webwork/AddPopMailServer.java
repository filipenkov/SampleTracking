/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.util.collect.MapBuilder;

import java.util.Map;

public class AddPopMailServer extends AddMailServer
{
    @Override
    public Map<String, String> getSupportedServiceProviders()
    {
        return MapBuilder.<String, String>newBuilder()
            .add("custom", getText("admin.mailservers.custom"))
            .add("gmail-pop3", "Google Apps Mail / Gmail (POP3)")
            .add("gmail-imap", "Google Apps Mail / Gmail (IMAP)")
            .add("yahooplus", "Yahoo! Mail Plus")
            .toLinkedHashMap();
    }

    @Override
    public String getActiveTab()
    {
        return ViewMailServers.INCOMING_MAIL_TAB;
    }

    @Override
    public String getCancelURI()
    {
        return ViewMailServers.INCOMING_MAIL_ACTION;
    }
}

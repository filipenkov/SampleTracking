/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;

import java.util.Map;

public class AddSmtpMailServer extends AddMailServer
{

    public static Map<String, String> getSupportedServiceProviders(I18nHelper i18nHelper)
    {
        return MapBuilder.<String, String>newBuilder()
            .add("custom", i18nHelper.getText("admin.mailservers.custom"))
            .add("gmail-smtp", "Google Apps Mail / Gmail")
            .add("yahooplus-smtp", "Yahoo! Mail Plus")
            .toLinkedHashMap();
    }

    @Override
    public Map<String, String> getSupportedServiceProviders()
    {
        return getSupportedServiceProviders(this);
    }

    @Override
    public String getActiveTab()
    {
        return ViewMailServers.OUTGOING_MAIL_TAB;
    }

    @Override
    public String getCancelURI()
    {
        return ViewMailServers.OUTGOING_MAIL_ACTION;
    }
}

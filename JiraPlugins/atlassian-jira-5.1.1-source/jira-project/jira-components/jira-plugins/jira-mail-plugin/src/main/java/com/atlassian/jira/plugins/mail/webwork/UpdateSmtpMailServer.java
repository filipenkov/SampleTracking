/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

public class UpdateSmtpMailServer extends UpdateMailServer
{
    @Override
    public String getActiveTab()
    {
        return ViewMailServers.OUTGOING_MAIL_TAB;
    }

    @Override
    public String getCancelURI()
    {
        return ViewMailServers.OUTGOING_MAIL_ACTION;
    }}

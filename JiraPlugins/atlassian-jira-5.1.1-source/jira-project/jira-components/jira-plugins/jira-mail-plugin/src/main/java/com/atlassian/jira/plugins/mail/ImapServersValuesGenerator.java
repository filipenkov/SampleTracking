/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;

public class ImapServersValuesGenerator implements ValuesGenerator
{
    public Map<String, String> getValues(Map params)
    {
        final Map<String, String> returnValues = newHashMap();
        final Iterable<PopMailServer> mailServers = filter(getMailServerManager().getPopMailServers(), getPredicate());
        for (PopMailServer mailServer : mailServers)
        {
            returnValues.put(mailServer.getId().toString(), mailServer.getName());
        }
        return returnValues;
    }

    protected MailServerManager getMailServerManager()
    {
        return MailFactory.getServerManager();
    }

    protected Predicate<? super PopMailServer> getPredicate()
    {
        return new Predicate<PopMailServer>()
        {
            @Override
            public boolean apply(@Nullable PopMailServer input)
            {
                return input != null
                        && (input.getMailProtocol().equals(MailProtocol.SECURE_IMAP)
                        || input.getMailProtocol().equals(MailProtocol.IMAP));
            }
        };
    }
}

/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.managers.AbstractMailServerManager;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class MockMailServerManager extends AbstractMailServerManager
{
    List<MailServer> mailServers = Lists.newArrayList();
    private long idSequence = 1000;

    @Override
    public MailServer getMailServer(final Long id) throws MailException
    {
        return (MailServer) CollectionUtils.find(mailServers, new Predicate()
        {
            @Override
            public boolean evaluate(Object object)
            {
                return ((MailServer) object).getId().longValue() == id;
            }
        });
    }

    @Override
    public MailServer getMailServer(final String name) throws MailException
    {
        return (MailServer) CollectionUtils.find(mailServers, new Predicate()
        {
            @Override
            public boolean evaluate(Object object)
            {
                return StringUtils.equals(((MailServer) object).getName(), name);
            }
        });
    }

    @Override
    public List<String> getServerNames() throws MailException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SMTPMailServer> getSmtpMailServers()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PopMailServer> getPopMailServers()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long create(MailServer mailServer) throws MailException
    {
        mailServer.setId(idSequence++);
        mailServers.add(mailServer);
        return mailServer.getId();
    }

    @Override
    public void update(MailServer mailServer) throws MailException
    {
        delete(mailServer.getId());
        mailServers.add(mailServer);
    }

    @Override
    public void delete(final Long mailServerId) throws MailException
    {
        Iterables.removeIf(mailServers, new com.google.common.base.Predicate<MailServer>()
        {
            @Override
            public boolean apply(MailServer input)
            {
                return input.getId().longValue() == mailServerId;
            }
        });
    }

    @Override
    public SMTPMailServer getDefaultSMTPMailServer()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PopMailServer getDefaultPopMailServer()
    {
        throw new UnsupportedOperationException();
    }
}

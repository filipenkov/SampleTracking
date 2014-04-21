/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail;

import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.PopMailServer;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

public class PopServersValuesGenerator extends ImapServersValuesGenerator
{
    @Override
    protected Predicate<? super PopMailServer> getPredicate()
    {
        return new Predicate<PopMailServer>()
        {
            @Override
            public boolean apply(@Nullable PopMailServer input)
            {
                return input != null
                        && (input.getMailProtocol().equals(MailProtocol.SECURE_POP) || input.getMailProtocol().equals(MailProtocol.POP));
            }
        };
    }
}

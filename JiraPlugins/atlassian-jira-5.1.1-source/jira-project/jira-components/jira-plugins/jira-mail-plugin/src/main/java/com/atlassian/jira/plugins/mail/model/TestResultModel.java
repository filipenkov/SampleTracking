/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.model;

import com.atlassian.jira.service.util.handler.MessageHandlerStats;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class TestResultModel
{
    private final boolean succeeded;
    private final MessageHandlerStats stats;
    private final LinkedHashSet<String> errors;
    private final List<String> dryRunMessages;

    public TestResultModel(boolean succeeded, Collection<String> errors, @Nullable MessageHandlerStats stats,
            List<String> dryRunMessages) {

        this.succeeded = succeeded;
        this.stats = stats;
        this.errors = Sets.newLinkedHashSet(errors);
        this.dryRunMessages = dryRunMessages;
    }

    public boolean isSucceeded()
    {
        return succeeded;
    }

    public MessageHandlerStats getStats()
    {
        return stats;
    }

    public LinkedHashSet<String> getErrors()
    {
        return errors;
    }

    public List<String> getDryRunMessages()
    {
        return dryRunMessages;
    }
}

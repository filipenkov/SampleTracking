package com.atlassian.jira.plugins.mail;

import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.mail.Message;
import java.util.List;

/**
 * MessageHandlerExecutionMonitor used in test run.
 */
public class DryRunMessageHandlerExecutionMonitor implements MessageHandlerExecutionMonitor
{
    private final List<String> dryRunMessages = Lists.newArrayList();
    private final List<String> errorMessages = Lists.newArrayList();

    private int numMessages;
    private int numMessagesRejected;

    @Override
    public void error(String error, @Nullable Throwable e)
    {
        final String s = error + (e != null ? ": " + e.getMessage() : "");
        dryRunMessages.add(s);
        errorMessages.add(s);
    }

    @Override
    public void error(String error)
    {
        dryRunMessages.add(error);
        errorMessages.add(error);
    }

    @Override
    public void info(String info)
    {
        dryRunMessages.add(info);
    }

    @Override
    public void info(String info, @Nullable Throwable e)
    {
        dryRunMessages.add(info + (e != null ? ": " + e.getMessage() : ""));
    }

    @Override
    public void warning(String warning)
    {
        error(warning);
    }

    @Override
    public void warning(String warning, @Nullable Throwable e)
    {
        error(warning, e);
    }

    public boolean hasErrors()
    {
        return !errorMessages.isEmpty();
    }

    @Override
    public void messageRejected(Message message, String reason)
    {
        numMessagesRejected++;
    }

    @Override
    public void nextMessage(Message message)
    {
    }

    public List<String> getAllMessages()
    {
        return dryRunMessages;
    }

    @Override
    public void setNumMessages(int count)
    {
        numMessages = count;
    }

    public int getNumMessages()
    {
        return numMessages;
    }

    public int getNumMessagesRejected()
    {
        return numMessagesRejected;
    }

    public Iterable<String> getErrorMessages()
    {
        return errorMessages;
    }

}



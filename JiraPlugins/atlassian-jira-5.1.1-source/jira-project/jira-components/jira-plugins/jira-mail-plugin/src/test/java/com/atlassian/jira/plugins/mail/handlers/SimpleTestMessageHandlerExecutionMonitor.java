package com.atlassian.jira.plugins.mail.handlers;


import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;

import javax.annotation.Nullable;
import javax.mail.Message;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * An ancient class which used to handle errors from mail handlers. Now MessageHandlerExecutionMonitor is used instead
 * and everyone is welcome to provide their own implementation. This naive implementation is still used by unit tests.
 */
public class SimpleTestMessageHandlerExecutionMonitor implements MessageHandlerExecutionMonitor
{
    private String errorMessage;
    private String exception;

    public SimpleTestMessageHandlerExecutionMonitor()
    {
        errorMessage = null;
        exception = null;
    }

    public boolean hasErrors()
    {
        return errorMessage != null || exception != null;
    }

    public String getErrorsAsString()
    {
        return errorMessage;
    }

    public String getExceptionsAsString()
    {
        return exception;
    }

    public void error(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    @Override
    public void messageRejected(Message message, String reason)
    {
    }

    @Override
    public void nextMessage(Message message)
    {
        reset();
    }

    public void error(String errorMessage, @Nullable Throwable exception)
    {
        this.errorMessage = errorMessage;

        if (exception == null)
        {
            this.exception = null;
            return;
        }

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter p = new PrintWriter(stringWriter);
        exception.printStackTrace(p);
        p.flush();
        this.exception = stringWriter.toString();
    }

    @Override
    public void info(String info)
    {
    }

    @Override
    public void info(String info, @Nullable Throwable e)
    {
    }

    @Override
    public void warning(String warning)
    {
    }

    @Override
    public void warning(String warning, @Nullable Throwable e)
    {
    }

    public void reset()
    {
        errorMessage = null;
        exception = null;
    }

    public String getError()
    {
        return getErrorsAsString();
    }

    @Override
    public void setNumMessages(int count)
    {
    }


}

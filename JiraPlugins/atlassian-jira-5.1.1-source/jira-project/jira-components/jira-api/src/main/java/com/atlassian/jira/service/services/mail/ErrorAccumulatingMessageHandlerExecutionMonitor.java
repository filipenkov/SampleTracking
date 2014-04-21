package com.atlassian.jira.service.services.mail;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.mail.Message;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Internal-use class only
 *
 * @since v5.0
 */
@Internal
class ErrorAccumulatingMessageHandlerExecutionMonitor implements MessageHandlerExecutionMonitor
{
    private final MessageHandlerExecutionMonitor delgate;
    private volatile boolean hasErrors = false;
    private final Queue<String> errors = Lists.newLinkedList();
    private String lastReportedExceptionStackTrace;


    public ErrorAccumulatingMessageHandlerExecutionMonitor(MessageHandlerExecutionMonitor delgate)
    {
        this.delgate = delgate;
    }

    @Override
    public void setNumMessages(int count)
    {
        delgate.setNumMessages(count);
    }

    @Override
    public void messageRejected(Message message, String reason)
    {
        delgate.messageRejected(message, reason);
    }

    @Override
    public void nextMessage(Message message)
    {
        delgate.nextMessage(message);
    }


    @Override
    public void error(String error)
    {
        delgate.error(error);
        addErrorImpl(error);

    }

    @Override
    public void warning(String warning)
    {
        delgate.warning(warning);
        addErrorImpl(warning);
    }

    @Override
    public void warning(String warning, @Nullable Throwable e)
    {
        delgate.warning(warning, e);
        addErrorImpl(warning);
    }

    @Override
    public void info(String info)
    {
        delgate.info(info);
    }

    @Override
    public void info(String info, @Nullable Throwable e)
    {
        delgate.info(info, e);
    }


    private synchronized void addErrorImpl(String error)
    {
        hasErrors = true;
        if (error != null)
        {
            if (errors.size() > 5)
            {
                errors.remove();
            }
            errors.add(error);
        }
    }


    @Override
    public void error(String error, @Nullable Throwable e)
    {
        delgate.error(error, e);
        addErrorImpl(error);
        if (e != null)
        {
            synchronized (this) {
                lastReportedExceptionStackTrace = getStackTraceAsString(e);
            }
        }
        else
        {
            synchronized (this) {
                lastReportedExceptionStackTrace = null;
            }
        }
    }

    public synchronized boolean hasErrors()
    {
        return hasErrors;
    }

    public synchronized String getErrorsAsString()
    {
        return StringUtils.join(errors, "\n");
    }

    public synchronized String getExceptionsAsString()
    {
        return lastReportedExceptionStackTrace;
    }

    String getStackTraceAsString(Throwable e)
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter p = new PrintWriter(stringWriter);
        e.printStackTrace(p);
        p.flush();
        p.close();
        return stringWriter.toString();
    }

}

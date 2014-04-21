package com.atlassian.mail.queue;

import com.atlassian.mail.MailThreader;

import java.util.Date;

public abstract class AbstractMailQueueItem implements MailQueueItem
{
    String subject;
    Date dateQueued;
    private int timesSent = 0;
    protected MailThreader mailThreader;
    

    public AbstractMailQueueItem()
    {
        this.dateQueued = new Date();
    }

    public AbstractMailQueueItem(String subject)
    {
        this();
        this.subject = subject;
    }

    public String getSubject()
    {
        return subject;
    }

    public Date getDateQueued()
    {
        return dateQueued;
    }

    public int getSendCount()
    {
        return timesSent;
    }

    public boolean hasError()
    {
        return (timesSent > 0);
    }

    protected void incrementSendCount()
    {
        timesSent++;
    }

    public void setMailThreader(MailThreader mailThreader) {
        this.mailThreader = mailThreader;
    }

    public void execute() throws Exception
    {
        send();
    }

    final public int compareTo(MailQueueItem o)
    {
        int priorityComparator = new Integer(timesSent).compareTo(o.getSendCount());
        return priorityComparator == 0 ? dateQueued.compareTo(o.getDateQueued()) : priorityComparator;
    }
}

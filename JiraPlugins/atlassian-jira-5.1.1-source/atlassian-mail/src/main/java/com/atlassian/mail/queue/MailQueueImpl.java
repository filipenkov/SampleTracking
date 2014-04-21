/**
 * Created by IntelliJ IDEA.
 * User: Mike Cannon-Brookes
 * Date: Jan 22, 2003
 * Time: 12:39:32 AM
 * To change this template use Options | File Templates.
 */
package com.atlassian.mail.queue;

import com.atlassian.mail.MailException;
import org.apache.log4j.Category;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This is a volatile queue of the outgoing emails from JIRA.
 * <p/>
 * Note - this class may lose emails if the server shuts down.
 */
public class MailQueueImpl implements MailQueue
{
    private static final Category log = Category.getInstance(MailQueueImpl.class);
    private static final int MAX_SEND_ATTEMPTS = 10;
    private Queue<MailQueueItem> items;
    private Queue<MailQueueItem> errorItems;
    private boolean sending;
    private MailQueueItem itemBeingSent;
    private Timestamp sendingStarted;

    public MailQueueImpl()
    {
        items = new PriorityBlockingQueue<MailQueueItem>();
        errorItems = new PriorityBlockingQueue<MailQueueItem>();
    }

    public void sendBuffer()
    {
        if (sending)
        {
            log.warn("Already sending "+items.size()+" mails:");
            for (final MailQueueItem item : items)
            {
                log.warn("Queued to send: " + item + ", " + item.getClass());
            }
            return;
        }

        sendingStarted();
        List<MailQueueItem> failed = new ArrayList<MailQueueItem>();

        try
        {
            while (!items.isEmpty())
            {
                String origThreadName = Thread.currentThread().getName();
                MailQueueItem item = items.poll();
                this.itemBeingSent = item;
                log.debug("Sending: " + item);
                try
                {
                    Thread.currentThread().setName("Sending mailitem "+item);
                    item.send();
                }
                catch (MailException e)
                {
                    if (item.getSendCount() > MAX_SEND_ATTEMPTS)
                        errorItems.add(item);
                    else
                        failed.add(item);

                    log.error("Error occurred in sending e-mail: " + item, e);
                }
                finally
                {
                    Thread.currentThread().setName(origThreadName);
                }
            }

            items.addAll(failed);
        }
        finally
        {
            // Set sending to false no matter what happens
            sendingStopped();
        }
    }

    public int size()
    {
        return items.size();
    }

    public int errorSize()
    {
        return errorItems.size();
    }

    public void addItem(MailQueueItem item)
    {
        log.debug("Queued: " + item);
        items.add(item);
    }

    public void addErrorItem(MailQueueItem item)
    {
        log.debug("Queued error: " + item);
        errorItems.add(item);
    }

    public Queue<MailQueueItem> getQueue()
    {
        return items;
    }

    public Queue<MailQueueItem> getErrorQueue()
    {
        return errorItems;
    }

    public boolean isSending()
    {
        return sending;
    }

    public Timestamp getSendingStarted()
    {
        return sendingStarted;
    }

    public MailQueueItem getItemBeingSent()
    {
        return itemBeingSent;
    }

    public void unstickQueue() {
        log.error("Mail on queue was considered stuck: " + itemBeingSent);
        sendingStopped();
    }

    public void emptyErrorQueue()
    {
        errorItems.clear();
    }

    public void resendErrorQueue()
    {
        items.addAll(errorItems);
        emptyErrorQueue();
    }

    public void sendingStarted()
    {
        sending = true;
        sendingStarted = new Timestamp(System.currentTimeMillis());
    }

    public void sendingStopped()
    {
        sending = false;
        sendingStarted = null;
    }
}
package com.atlassian.mail.queue;

import com.atlassian.core.task.Task;
import com.atlassian.mail.MailException;

/**
 * Created by IntelliJ IDEA.
 * User: Mike Cannon-Brookes
 * Date: Jan 22, 2003
 * Time: 12:55:18 AM
 * To change this template use Options | File Templates.
 */
public interface MailQueueItem extends Task, Comparable<MailQueueItem>
{
    /**
     * Attempt to send the  mail item.
     * @throws com.atlassian.mail.MailException If there is a problem sending the mail item
     */
    void send() throws MailException;

    /**
     * @return String the Subject of the mail item.
     */
    public String getSubject();

    /**
     * @return java.util.Date the date the item was last put in the queue
     */
    public java.util.Date getDateQueued();

    /**
     * @return int the number of times the item has attempted to be sent
     */
    public int getSendCount();

    /**
     * @return If there was a problem sending the item
     */
    public boolean hasError();
}

package com.atlassian.mail.queue;

import java.sql.Timestamp;
import java.util.Queue;

/**
 * The MailQueue is used to asynchronously send mail from within applications.
 * <p/>
 * It is populated with {@link MailQueueItem} objects, which know how to send themselves.
 * <p/>
 * The queue also keeps track of which messages have attempted to be send, and were erroring.
 */
public interface MailQueue
{
    /**
     * Send all the messages in the queue.
     */
    void sendBuffer();

    /**
     * @return The number of messages in the queue.
     */
    int size();

    /**
     * @return The number of erroring messages in the queue.
     */
    int errorSize();

    /**
     * Add a new item to the mail queue.
     *
     * @param item The item to be added to the queue.
     */
    void addItem(MailQueueItem item);

    /**
     * Add an error item
     *
     * @param item
     */
    void addErrorItem(MailQueueItem item);

    /**
     * Get access to the messages in the mail queue.
     * <p/>
     * Note: you should synchronize on access to this queue.
     */
    Queue<MailQueueItem> getQueue();

    /**
     * Get access to the messages in the error queue.
     * <p/>
     * Note: you should synchronize on access to this queue.
     */
    Queue<MailQueueItem> getErrorQueue();

    /**
     * @return Whether or not the queue is currently sending.
     */
    boolean isSending();

    /**
     * @return The date/time the queue started sending, null if the queue is not sending
     */
    Timestamp getSendingStarted();

    /**
     * Empty the error queue (discard these messages)
     */
    void emptyErrorQueue();

    /**
     * Send all messages in the error queue.
     */
    void resendErrorQueue();

    /**
     * Retrieve the item currently being sent.
     */
    MailQueueItem getItemBeingSent();

    /**
     * If the queue is sending and has 'stuck' on an item, this lets the queue proceed.
     * Only relevant for synchronous queues.
     */
    void unstickQueue();
}

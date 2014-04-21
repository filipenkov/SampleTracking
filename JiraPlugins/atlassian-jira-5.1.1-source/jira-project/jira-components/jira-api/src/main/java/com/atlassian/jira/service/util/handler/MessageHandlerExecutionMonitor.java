package com.atlassian.jira.service.util.handler;

import com.atlassian.annotations.PublicApi;

import javax.mail.Message;

/**
 * This is extension to MessageHandlerErrorCollector interface for
 * with a few reporting methods around Messages.
 *
 *
 * You should not need to implement this interface (unless for test purposes).
 */
@PublicApi
public interface MessageHandlerExecutionMonitor extends MessageHandlerErrorCollector
{
    /**
     * Report the number of messages ready for processing by the handler in the current run
     * @param count number of messages ready for processing by the handler in the current run
     */
    void setNumMessages(int count);

    /**
     * Reports the rejection of the message
     * @param message the message which has been rejected (should be the one reported before with nextMessage() method.
     * @param reason the reason of the rejection (may be displayed in the web UI, so translated messages
     * are desirable here)
     */
    void messageRejected(Message message, String reason);

    /**
     * Reports that given message is about to be dispatched to the message handler.
     * @param message the message which is about to be dispatched to the handler.
     */
    void nextMessage(Message message);
}

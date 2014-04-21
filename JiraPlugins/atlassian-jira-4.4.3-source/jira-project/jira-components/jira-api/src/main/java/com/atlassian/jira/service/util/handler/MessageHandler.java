/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util.handler;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Map;

/**
 * An interface representing a message handler. A handler handles messages. Implementers should extend
 * AbstractMessageHandler to inherit standard functionality such mail loop detection etc.
 * <p>
 * All implementations of MessageHandler need a no-arg constructor, as they are instantiated via reflection.
 */
public interface MessageHandler
{
    /**
     * Will be called before any messages are to be handled.
     * @param params configuration.
     */
    void init(Map params);

    /**
     * Perform the specific work of this handler for the given message.
     *
     * @param message the message to check for handling.
     * @return true if the message is to be deleted from the source.
     * @throws MessagingException if anything went wrong.
     */
    boolean handleMessage(Message message) throws MessagingException;

    void setErrorHandler(MessageErrorHandler errorHandler);

}

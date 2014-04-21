package com.atlassian.event.config;

import com.atlassian.event.spi.ListenerHandler;

import java.util.List;

/**
 * Specifies a listener handler configuration to use
 */
public interface ListenerHandlersConfiguration
{
    List<ListenerHandler> getListenerHandlers();
}

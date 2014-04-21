package com.atlassian.streams.action;

/**
 * Locates attributes of action handlers enabled in the system.
 */
public interface ActionHandlerAccessor
{
    /**
     * Returns all module keys associated with any {@code ActionHandlersModuleDescriptor} currently enabled in the system.
     * 
     * @return all module keys associated with any {@code ActionHandlersModuleDescriptor} currently enabled in the system.
     */
    Iterable<String> getActionHandlerModuleKeys();
}

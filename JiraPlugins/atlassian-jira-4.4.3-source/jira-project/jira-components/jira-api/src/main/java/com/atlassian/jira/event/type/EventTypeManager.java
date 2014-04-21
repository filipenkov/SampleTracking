package com.atlassian.jira.event.type;

import org.apache.commons.collections.MultiMap;

import java.util.Collection;
import java.util.Map;

public interface EventTypeManager
{
    Collection getEventTypes();

    Map getEventTypesMap();

    EventType getEventType(Long id);

    boolean isActive(EventType eventType);

    MultiMap getAssociatedWorkflows(EventType eventType, boolean statusCheck);

    Map getAssociatedNotificationSchemes(EventType eventType);

    void addEventType(EventType eventType);

    void editEventType(Long eventTypeId, String name, String description, Long templateId);

    void deleteEventType(Long eventTypeId);

    boolean isEventTypeExists(String eventTypeName);

    boolean isEventTypeExists(Long eventTypeId);

    void clearCache();
}

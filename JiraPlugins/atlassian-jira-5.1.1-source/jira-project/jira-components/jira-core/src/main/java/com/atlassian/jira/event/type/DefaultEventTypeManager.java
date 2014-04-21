/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.type;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.EventTypeOrderTransformer;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages event types within the system.
 * <p/>
 * Used to add, edit, delete and retrieve event types.
 */
@EventComponent
public class DefaultEventTypeManager implements EventTypeManager
{
    private static final Logger log = Logger.getLogger(DefaultEventTypeManager.class);

    public static final String EVENT_TYPE_ID = "eventTypeId";

    private final OfBizDelegator delegator;
    private final WorkflowManager workflowManager;
    private final NotificationSchemeManager notificationSchemeManager;

    private Collection eventTypes;
    private Map eventTypesMap;
    private final Comparator eventTypeComparator = new TransformingComparator(new EventTypeOrderTransformer());

    public DefaultEventTypeManager(OfBizDelegator delegator, WorkflowManager workflowManager, NotificationSchemeManager notificationSchemeManager)
    {
        this.delegator = delegator;
        this.workflowManager = workflowManager;
        this.notificationSchemeManager = notificationSchemeManager;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearCache();
    }

    // ---- Retrieval methods ------------------------------------------------------------------------------------------

    public Collection getEventTypes()
    {
        if (eventTypes == null)
        {
            eventTypes = getEventTypesMap().values();
        }

        return eventTypes;
    }

    /**
     * Retrieve a map of all event types.
     *
     * @return eventTypesMap    a map of eventTypeIds -> eventTypes
     */
    public synchronized Map getEventTypesMap()
    {
        if (eventTypesMap == null)
        {
            eventTypesMap = new ListOrderedMap();
            Collection allEventTypes = retrieveAllEntities();

            for (Iterator iterator = allEventTypes.iterator(); iterator.hasNext();)
            {
                EventType eventType = (EventType) iterator.next();
                eventTypesMap.put(eventType.getId(), eventType);
            }
        }

        return eventTypesMap;
    }

    /**
     * Retrieve the event type by the specified id.
     *
     * @param id ID
     * @return EventType    the event type with the specified id.
     */
    public EventType getEventType(Long id)
    {
        if (getEventTypesMap().containsKey(id))
        {
            return (EventType) getEventTypesMap().get(id);
        }
        else
        {
            GenericValue issueEventTypeGV = retrieveEntityByPrimaryKey(EasyMap.build("id", id));
            if (issueEventTypeGV == null)
            {
                throw new IllegalArgumentException("No event type with id " + id);
            }
            return new EventType(issueEventTypeGV);
        }
    }

    // ---- Event Type specific methods --------------------------------------------------------------------------------

    /**
     * Determine if the EventType specified is associated with a workflow and/or notification scheme.
     *
     * @param eventType event type
     * @return true     if event type is associated with a workflow and/or notification scheme.
     */
    public boolean isActive(EventType eventType)
    {
        return !(getAssociatedWorkflows(eventType, true).isEmpty() && getAssociatedNotificationSchemes(eventType).isEmpty());
    }

    /**
     * Determines which workflows and transitions are associated with the specified eventType.
     * <p/>
     * The event type can be associated with a workflow through a post function on any of the workflow transitions.
     *
     * @param eventType   event type
     * @param statusCheck option to break on first association discovered - used when checking if event type is active
     * @return MultiMap     {@link com.atlassian.jira.web.action.issue.bulkedit.WorkflowTransitionKey}s -> transitions
     */
    public MultiMap getAssociatedWorkflows(EventType eventType, boolean statusCheck)
    {
        MultiMap workflowTransitionMap = new MultiHashMap();

        Collection<JiraWorkflow> workflows = workflowManager.getWorkflows();
        Long eventTypeId = eventType.getId();

        for (final JiraWorkflow workflow : workflows)
        {
            Map<ActionDescriptor, Collection<FunctionDescriptor>> transitionPostFunctionMap = workflowManager.getPostFunctionsForWorkflow(workflow);

            Collection<ActionDescriptor> keys = transitionPostFunctionMap.keySet();

            for (final ActionDescriptor actionDescriptor : keys)
            {
                Collection<FunctionDescriptor> postFunctions = transitionPostFunctionMap.get(actionDescriptor);

                for (final FunctionDescriptor functionDescriptor : postFunctions)
                {
                    if (functionDescriptor.getArgs().containsKey(EVENT_TYPE_ID) &&
                            eventTypeId.equals(new Long((String) functionDescriptor.getArgs().get(EVENT_TYPE_ID))))
                    {
                        workflowTransitionMap.put(workflow.getName(), actionDescriptor);

                        // Exit now as we only need one association for a status check
                        if (statusCheck)
                        {
                            return workflowTransitionMap;
                        }
                    }
                }
            }
        }

        return workflowTransitionMap;
    }

    /**
     * Return a collection of notificiation scheme names that the specified eventType is associated with.
     * <p/>
     * The event type can be associated with a notification scheme if the scheme has at least one notification type and
     * template selected for the event type.
     *
     * @param eventType event type
     * @return Collection       a collection of notificiation scheme names
     */
    public Map getAssociatedNotificationSchemes(EventType eventType)
    {
        return notificationSchemeManager.getSchemesMapByConditions(EasyMap.build(EVENT_TYPE_ID, eventType.getId()));
    }

    // ---- Add, Edit, Delete methods ----------------------------------------------------------------------------------

    public void addEventType(EventType eventType)
    {
        Map params = new HashMap();
        // Set an ID
        params.put("id", new Long(getNextEventTypeId()));
        params.put("name", eventType.getName());
        params.put("description", eventType.getDescription());
        params.put("templateId", eventType.getTemplateId());
        params.put("type", null);

        delegator.createValue(EventType.EVENT_TYPE, params);

        clearCache();
    }

    public void editEventType(Long eventTypeId, String name, String description, Long templateId)
    {
        GenericValue eventTypeGV = retrieveEntityByPrimaryKey(EasyMap.build("id", eventTypeId));
        eventTypeGV.set("name", name);
        eventTypeGV.set("description", description);
        eventTypeGV.set("templateId", templateId);
        delegator.store(eventTypeGV);

        clearCache();
    }

    public void deleteEventType(Long eventTypeId)
    {
        Map params = new HashMap();
        params.put("id", eventTypeId);
        delegator.removeByAnd(EventType.EVENT_TYPE, params);

        clearCache();
    }

    // ---- Validation methods -----------------------------------------------------------------------------------------

    public boolean isEventTypeExists(String issueEventTypeName)
    {
        if (issueEventTypeName == null)
        {
            throw new IllegalArgumentException("EventTypeName must not be null.");
        }

        for (Iterator iterator = getEventTypes().iterator(); iterator.hasNext();)
        {
            EventType eventType = (EventType) iterator.next();
            if (issueEventTypeName.equals(eventType.getName()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isEventTypeExists(Long eventTypeId)
    {
        if (eventTypeId == null)
        {
            throw new IllegalArgumentException("EventTypeId must not be null.");
        }

        Collection keySet = getEventTypesMap().keySet();

        return keySet.contains(eventTypeId);
    }

    // ---- Database methods -------------------------------------------------------------------------------------------
    /**
     * Return a list of {@link com.atlassian.jira.event.type.EventType}s extracted from the database using the specified params.
     *
     * @return Collection   all event types within the system.
     */
    private Collection retrieveAllEntities()
    {
        List eventTypes = new ArrayList();

        Collection eventTypeGVs = delegator.findAll(EventType.EVENT_TYPE);

        for (Iterator iterator = eventTypeGVs.iterator(); iterator.hasNext();)
        {
            GenericValue eventTypeGV = (GenericValue) iterator.next();
            EventType eventType = new EventType(eventTypeGV);
            eventTypes.add(eventType);
        }

        Collections.sort(eventTypes, eventTypeComparator);

        return eventTypes;
    }

    private GenericValue retrieveEntityByPrimaryKey(Map params)
    {
        return delegator.findByPrimaryKey(EventType.EVENT_TYPE, params);
    }

    /**
     * Calculate a new entity ID (by basically taking one more than the max integer that exists).
     * If there are string IDs here, they will not be affected.
     *
     * @return long     the next available event type id.
     */
    private synchronized long getNextEventTypeId()
    {
        long startID = 10000;

        List entities;
        entities = ComponentAccessor.getOfBizDelegator().findAll(EventType.EVENT_TYPE);

        for (Iterator iterator = entities.iterator(); iterator.hasNext();)
        {
            GenericValue entity = (GenericValue) iterator.next();
            long entityId = entity.getLong("id").longValue();
            if (entityId >= startID)
            {
                startID = entityId + 1;
            }
        }

        return startID;
    }

    // ---- Helper methods ---------------------------------------------------------------------------------------------

    public synchronized void clearCache()
    {
        eventTypes = null;
        eventTypesMap = null;
    }
}

package com.atlassian.jira.issue.fields.screen;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class CachingFieldScreenStore implements FieldScreenStore, Startable
{
    private final FieldScreenStore decoratedStore;
    private final EventPublisher eventPublisher;
    private volatile Map<Long, FieldScreen> fieldScreenMap;
    private volatile List<FieldScreen> fieldScreenList;

    public CachingFieldScreenStore(FieldScreenStore decoratedStore, final EventPublisher eventPublisher)
    {
        this.decoratedStore = decoratedStore;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public void setFieldScreenManager(FieldScreenManager fieldScreenManager)
    {
        decoratedStore.setFieldScreenManager(fieldScreenManager);
    }

    public FieldScreen getFieldScreen(Long id)
    {
        return fieldScreenMap.get(id);
    }

    public List<FieldScreen> getFieldScreens()
    {
        // return the cached immutable list. 
        return fieldScreenList;
    }

    public void createFieldScreen(FieldScreen fieldScreen)
    {
        decoratedStore.createFieldScreen(fieldScreen);
        refresh();
    }

    public void removeFieldScreen(Long id)
    {
        decoratedStore.removeFieldScreen(id);
        refresh();
    }

    public void updateFieldScreen(FieldScreen fieldScreen)
    {
        decoratedStore.updateFieldScreen(fieldScreen);
        refresh();
    }

    public void createFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        decoratedStore.createFieldScreenTab(fieldScreenTab);
    }

    public void updateFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        decoratedStore.updateFieldScreenTab(fieldScreenTab);
    }

    public List<FieldScreenTab> getFieldScreenTabs(FieldScreen fieldScreen)
    {
        return decoratedStore.getFieldScreenTabs(fieldScreen);
    }

    public void updateFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.updateFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void removeFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.removeFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void removeFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        decoratedStore.removeFieldScreenLayoutItems(fieldScreenTab);
    }

    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        return decoratedStore.getFieldScreenLayoutItems(fieldScreenTab);
    }

    public synchronized void refresh()
    {
        decoratedStore.refresh();
        populateCache();
    }

    public void createFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.createFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(GenericValue genericValue)
    {
        return decoratedStore.buildNewFieldScreenLayoutItem(genericValue);
    }

    public void removeFieldScreenTabs(FieldScreen fieldScreen)
    {
        decoratedStore.removeFieldScreenTabs(fieldScreen);
    }

    public void removeFieldScreenTab(Long id)
    {
        decoratedStore.removeFieldScreenTab(id);
    }

    public FieldScreenTab getFieldScreenTab(Long tabId)
    {
        // @TODO getting the tab by ID is not cached. Only used once at present
        return decoratedStore.getFieldScreenTab(tabId);
    }

    private void populateCache()
    {
        List<FieldScreen> fs = decoratedStore.getFieldScreens();
        Map<Long, FieldScreen> screenMap = new HashMap<Long, FieldScreen>();
        for (final FieldScreen fieldScreen : fs)
        {
            screenMap.put(fieldScreen.getId(), fieldScreen);
        }

        // Store this as an immutable cache. We will replace the cache on updates.
        fieldScreenMap = Collections.unmodifiableMap(screenMap);
        fieldScreenList = Collections.unmodifiableList(fs);
    }
}

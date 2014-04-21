package com.atlassian.jira.issue.fields.screen;

import org.apache.commons.collections.set.ListOrderedSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class DefaultFieldScreenManager implements FieldScreenManager
{
    private final FieldScreenStore fieldScreenStore;

    public DefaultFieldScreenManager(FieldScreenStore fieldScreenStore)
    {
        this.fieldScreenStore = fieldScreenStore;
        this.fieldScreenStore.setFieldScreenManager(this);
        this.fieldScreenStore.refresh();
    }

    public FieldScreen getFieldScreen(Long id)
    {
        return fieldScreenStore.getFieldScreen(id);
    }

    public Collection<FieldScreen> getFieldScreens()
    {
        return fieldScreenStore.getFieldScreens();
    }

    public Collection<FieldScreenTab> getFieldScreenTabs(String fieldId)
    {
        Set<FieldScreenTab> fieldScreenTabs = new ListOrderedSet();
        for (FieldScreen fieldScreen : getFieldScreens())
        {
            for (FieldScreenTab fieldScreenTab : fieldScreen.getTabs())
            {
                if (fieldScreenTab.getFieldScreenLayoutItem(fieldId) != null)
                {
                    fieldScreenTabs.add(fieldScreenTab);
                }
            }
        }

        return fieldScreenTabs;
    }

    public void createFieldScreen(FieldScreen fieldScreen)
    {
        fieldScreenStore.createFieldScreen(fieldScreen);
    }

    public void removeFieldScreen(Long id)
    {
        fieldScreenStore.removeFieldScreen(id);
    }

    public void updateFieldScreen(FieldScreen fieldScreen)
    {
        fieldScreenStore.updateFieldScreen(fieldScreen);
    }

    public void createFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        fieldScreenStore.createFieldScreenTab(fieldScreenTab);
    }

    public void updateFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        fieldScreenStore.updateFieldScreenTab(fieldScreenTab);
    }

    public FieldScreenTab getFieldScreenTab(Long fieldScreenTabId)
    {
        return fieldScreenStore.getFieldScreenTab(fieldScreenTabId);
    }

    public List<FieldScreenTab> getFieldScreenTabs(FieldScreen fieldScreen)
    {
        return fieldScreenStore.getFieldScreenTabs(fieldScreen);
    }

    public void createFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        fieldScreenStore.createFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void updateFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        fieldScreenStore.updateFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void removeFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        fieldScreenStore.removeFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void removeFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        fieldScreenStore.removeFieldScreenLayoutItems(fieldScreenTab);
    }

    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        return fieldScreenStore.getFieldScreenLayoutItems(fieldScreenTab);
    }

    public void removeFieldScreenItems(String fieldId)
    {
        // The field is being removed from the system (likely to be custom field :))
        // Remove references to this field from every field screen
        for (FieldScreen fieldScreen : getFieldScreens())
        {
            if (fieldScreen.containsField(fieldId))
            {
                fieldScreen.removeFieldScreenLayoutItem(fieldId);
            }
        }

    }

    public void refresh()
    {
        fieldScreenStore.refresh();
    }

    public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(String fieldId)
    {
        FieldScreenLayoutItem fieldScreenLayoutItem = fieldScreenStore.buildNewFieldScreenLayoutItem(null);
        fieldScreenLayoutItem.setFieldId(fieldId);
        return fieldScreenLayoutItem;
    }

    public void removeFieldScreenTabs(FieldScreen fieldScreen)
    {
        fieldScreenStore.removeFieldScreenTabs(fieldScreen);
    }

    public void removeFieldScreenTab(Long id)
    {
        fieldScreenStore.removeFieldScreenTab(id);
    }
}

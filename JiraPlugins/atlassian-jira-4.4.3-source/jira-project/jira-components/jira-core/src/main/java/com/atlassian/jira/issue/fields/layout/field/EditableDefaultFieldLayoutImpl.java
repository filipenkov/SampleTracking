/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.jira.issue.fields.OrderableField;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

public class EditableDefaultFieldLayoutImpl extends FieldLayoutImpl implements EditableDefaultFieldLayout
{
    public EditableDefaultFieldLayoutImpl(GenericValue genericValue, List<FieldLayoutItem> fieldLayoutItems)
    {
        super(genericValue, new ArrayList<FieldLayoutItem>(fieldLayoutItems));
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            super.init();
        }
        else
        {
            // Initialise to default values
            setId(null);
            setName(NAME);
            setDescription(DESCRIPTION);
        }
    }

    public void setDescription(final FieldLayoutItem fieldLayoutItem, final String description)
    {
        // If the descritpion is an empty string then set it to null
        String descriptionToSet = (description != null && description.length() == 0 ? null : description);
        FieldLayoutItem newfieldLayoutItem = new FieldLayoutItemImpl.Builder(fieldLayoutItem).setFieldDescription(descriptionToSet).build();
        List<FieldLayoutItem> internalList = getInternalList();
        int pos = internalList.indexOf(fieldLayoutItem);
        internalList.set(pos, newfieldLayoutItem);
    }

    public void setRendererType(final FieldLayoutItem fieldLayoutItem, final String rendererType)
    {
        final OrderableField field = fieldLayoutItem.getOrderableField();
        if (!getHackyFieldRendererRegistry().shouldOverrideDefaultRenderers(field) && !getFieldManager().isRenderableField(field))
        {
            throw new IllegalArgumentException("Trying to set a renderer on a field that is not renderable.");
        }
        FieldLayoutItem newfieldLayoutItem = new FieldLayoutItemImpl.Builder(fieldLayoutItem).setRendererType(rendererType).build();
        List<FieldLayoutItem> internalList = getInternalList();
        int pos = internalList.indexOf(fieldLayoutItem);
        internalList.set(pos, newfieldLayoutItem);
    }

    public String getType()
    {
        return FieldLayoutManager.TYPE_DEFAULT;
    }

    public void show(FieldLayoutItem fieldLayoutItem)
    {
        if (!getFieldManager().isHideableField(fieldLayoutItem.getOrderableField()))
        {
            throw new IllegalArgumentException("Trying to show a field that is not hideable.");
        }
        FieldLayoutItem fieldHide = new FieldLayoutItemImpl.Builder(fieldLayoutItem).setHidden(false).build();
        List<FieldLayoutItem> internalList = getInternalList();
        int pos = internalList.indexOf(fieldLayoutItem);
        internalList.set(pos, fieldHide);
    }

    public void hide(FieldLayoutItem fieldLayoutItem)
    {
        List<FieldLayoutItem> internalList = getInternalList();
        int hidePosition = internalList.indexOf(fieldLayoutItem);
        if (getFieldManager().isHideableField(fieldLayoutItem.getOrderableField()))
        {
            FieldLayoutItemImpl.Builder builder = new FieldLayoutItemImpl.Builder(fieldLayoutItem).setHidden(true);

            // When hiding a field make it optional (not required) if the field is not mandatory, this should not happen
            // since it makes not sense to have a hidden required field, but if a developer wants to be dumb, let them...
            builder.setRequired(getFieldManager().isMandatoryField(fieldLayoutItem.getOrderableField()));
            
            fieldLayoutItem = builder.build();

            internalList.set(hidePosition, fieldLayoutItem);
        }
        else
        {
            throw new IllegalArgumentException("Trying to hide a field that is not hideable.");
        }
    }

    public void makeRequired(FieldLayoutItem fieldLayoutItem)
    {
        List<FieldLayoutItem> internalList = getInternalList();
        int requiredPosition = internalList.indexOf(fieldLayoutItem);
        if (getFieldManager().isRequirableField(fieldLayoutItem.getOrderableField()))
        {
            // When requiring a field make it not hidden
            fieldLayoutItem = new FieldLayoutItemImpl.Builder(fieldLayoutItem).setHidden(false).setRequired(true).build();
            internalList.set(requiredPosition, fieldLayoutItem);
        }
        else
        {
            throw new IllegalArgumentException("Trying to require a field that is not requireable.");
        }
    }

    public void makeOptional(FieldLayoutItem fieldLayoutItem)
    {
        List<FieldLayoutItem> internalList = getInternalList();
        int optionalPositon = internalList.indexOf(fieldLayoutItem);
        if (getFieldManager().isRequirableField(fieldLayoutItem.getOrderableField()))
        {
            fieldLayoutItem = new FieldLayoutItemImpl.Builder(fieldLayoutItem).setRequired(false).build();
            internalList.set(optionalPositon, fieldLayoutItem);
        }
        else
        {
            throw new IllegalArgumentException("Trying to make optional a field that is not requireable.");
        }
    }

    public void setName(String name)
    {
        setInternalName(name);
    }

    public void setDescription(String description)
    {
        setInternalDescription(description);
    }

    public int hashCode()
    {
        return super.hashCode() + 29 * getInternalList().hashCode();
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof FieldLayout))
            return false;

        final FieldLayout fieldLayout = (FieldLayout) o;

        return super.equals(o) && getFieldLayoutItems().equals(fieldLayout.getFieldLayoutItems());
    }


}

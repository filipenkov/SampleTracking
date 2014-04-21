/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.text.DefaultTextRenderer;
import org.apache.commons.lang.builder.ToStringBuilder;

public class FieldLayoutItemImpl implements FieldLayoutItem
{
    private final OrderableField orderableField;
    private final String fieldDescription;
    private final boolean hidden;
    private final boolean required;
    private final String rendererType;
    private final FieldLayout fieldLayout;

    FieldLayoutItemImpl(OrderableField orderableField, String description, boolean hidden, boolean required, String rendererType, FieldLayout fieldLayout, FieldManager fieldManager)
    {
        if (hidden && !fieldManager.isHideableField(orderableField))
        {
            throw new IllegalArgumentException("Trying to create a Hidden Field Layout Item for a Field that is not hideable. '" + orderableField + "'.");
        }

        this.orderableField = orderableField;
        this.fieldDescription = description;
        this.hidden = hidden;
        this.required = required;
        this.rendererType = rendererType;
        this.fieldLayout = fieldLayout;
    }

    public OrderableField getOrderableField()
    {
        return orderableField;
    }

    public String getFieldDescription()
    {
        // JRA-10427 if we have a field description, prefer it over the default custom field description
        if (fieldDescription != null)
        {
            return fieldDescription;
        }

        FieldManager fieldManager = getFieldManager();
        if (fieldManager.isCustomField(getOrderableField()))
        {
            CustomField customField = fieldManager.getCustomField(getOrderableField().getId());
            return customField.getDescription();
        }
        return null;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public boolean isRequired()
    {
        return required;
    }

    public String getRendererType()
    {
        return rendererType;
    }

    public FieldLayout getFieldLayout()
    {
        return fieldLayout;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FieldLayoutItem))
        {
            return false;
        }

        final FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) o;

        if (hidden != fieldLayoutItem.isHidden())
        {
            return false;
        }
        if (required != fieldLayoutItem.isRequired())
        {
            return false;
        }
        if (orderableField != null ? !orderableField.equals(fieldLayoutItem.getOrderableField()) : fieldLayoutItem.getOrderableField() != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (orderableField != null ? orderableField.hashCode() : 0);
        result = 29 * result + (hidden ? 1 : 0);
        result = 29 * result + (required ? 1 : 0);
        return result;
    }

    public int compareTo(FieldLayoutItem fieldLayoutItem)
    {
        if (fieldLayoutItem == null)
        {
            return 1;
        }

        if (fieldLayoutItem.getOrderableField() == null)
        {
            if (getOrderableField() == null)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            if (getOrderableField() == null)
            {
                return -1;
            }
            else
            {
                return getOrderableField().compareTo(fieldLayoutItem.getOrderableField());
            }
        }
    }

    FieldManager getFieldManager()
    {
        return ManagerFactory.getFieldManager();
    }

    /**
     * Useful for instantiating {@link FieldLayoutItemImpl} objects from scratch or copying them.
     * 
     * @since v4.2
     */
    public static class Builder
    {
        private OrderableField orderableField = null;
        private String fieldDescription = null;
        private boolean hidden = false;
        private boolean required = false;
        private String rendererType = null;
        private FieldLayout fieldLayout = null;
        private FieldManager fieldManager = null;

        public Builder()
        {
        }

        /**
         * @param rhs the FieldLayoutItem instance to copy
         */
        public Builder(FieldLayoutItem rhs)
        {
            this.orderableField = rhs.getOrderableField();
            this.fieldDescription = rhs.getFieldDescription();
            this.hidden = rhs.isHidden();
            this.required = rhs.isRequired();
            this.rendererType = rhs.getRendererType();
            this.fieldLayout = rhs.getFieldLayout();
        }

        /**
         * @return a new {@link FieldLayoutItemImpl} instance. If a {@link FieldManager} was not set, the result of
         * {@link com.atlassian.jira.ManagerFactory#getFieldManager()} will be used.
         */
        public FieldLayoutItemImpl build()
        {
            if (fieldManager == null)
            {
                fieldManager = ManagerFactory.getFieldManager();
            }
            //init the renderertyp to a sensible default if it wasn't supplied explicitly.
            if(rendererType == null)
            {
                if(orderableField != null)
                {
                    final HackyFieldRendererRegistry hackyFieldRendererRegistry = ComponentManager.getComponentInstanceOfType(HackyFieldRendererRegistry.class);
                    if(hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(orderableField)) 
                    {
                        rendererType = hackyFieldRendererRegistry.getDefaultRendererType(orderableField).getKey();
                    }
                }
                if(rendererType == null)
                {
                    rendererType = DefaultTextRenderer.RENDERER_TYPE;
                }
            }
            return new FieldLayoutItemImpl(orderableField, fieldDescription, hidden, required, rendererType, fieldLayout, fieldManager);
        }

        public Builder setFieldDescription(final String fieldDescription)
        {
            this.fieldDescription = fieldDescription;
            return this;
        }

        public Builder setFieldLayout(final FieldLayout fieldLayout)
        {
            this.fieldLayout = fieldLayout;
            return this;
        }

        public Builder setHidden(final boolean hidden)
        {
            this.hidden = hidden;
            return this;
        }

        public Builder setOrderableField(final OrderableField orderableField)
        {
            this.orderableField = orderableField;
            return this;
        }

        public Builder setRendererType(final String rendererType)
        {
            this.rendererType = rendererType;
            return this;
        }

        public Builder setRequired(final boolean required)
        {
            this.required = required;
            return this;
        }

        public Builder setFieldManager(final FieldManager fieldManager)
        {
            this.fieldManager = fieldManager;
            return this;
        }
    }
}

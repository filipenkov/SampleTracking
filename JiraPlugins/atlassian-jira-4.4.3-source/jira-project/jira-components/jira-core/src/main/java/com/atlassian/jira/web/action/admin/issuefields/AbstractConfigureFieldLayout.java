/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public abstract class AbstractConfigureFieldLayout extends JiraWebActionSupport
{
    private Long id;
    Integer hide;
    Integer require;
    private FieldScreenManager fieldScreenManager;
    protected final HackyFieldRendererRegistry hackyFieldRendererRegistry;
    private Map fieldScreenTabMap;
    private List orderedList;
    private RendererManager rendererManager;
    private final ReindexMessageManager reindexMessageManager;
    private final FieldLayoutSchemeHelper fieldLayoutSchemeHelper;

    protected AbstractConfigureFieldLayout(FieldScreenManager fieldScreenManager, RendererManager rendererManager, final ReindexMessageManager reindexMessageManager,
            final FieldLayoutSchemeHelper fieldLayoutSchemeHelper, final HackyFieldRendererRegistry hackyFieldRendererRegistry)
    {
        this.fieldScreenManager = fieldScreenManager;
        this.hackyFieldRendererRegistry = hackyFieldRendererRegistry;
        this.fieldScreenTabMap = new HashMap();
        this.rendererManager = rendererManager;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.fieldLayoutSchemeHelper = notNull("fieldLayoutSchemeHelper", fieldLayoutSchemeHelper);
    }

    public abstract EditableFieldLayout getFieldLayout();

    public List getOrderedList()
    {
        if (orderedList == null)
        {
            orderedList = new ArrayList(getFieldLayout().getFieldLayoutItems());
            Collections.sort(orderedList);
        }

        return orderedList;
    }

    protected abstract String getFieldRedirect() throws Exception;

    @RequiresXsrfCheck
    public String doHide() throws Exception
    {
        doValidation();
        if (!invalidInput())
        {
            List fieldLayoutItems = getOrderedList();
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) fieldLayoutItems.get(getHide().intValue());
            if (fieldLayoutItem.isHidden())
            {
                // Not check if this field is hideable just incase the field has been hidden and they
                // need to show it again.
                getFieldLayout().show(fieldLayoutItem);
                store();
            }
            else
            {
                if (getFieldManager().isHideableField(fieldLayoutItem.getOrderableField()))
                {
                    getFieldLayout().hide(fieldLayoutItem);
                    store();
                }
                else
                {
                    addErrorMessage(getText("admin.errors.fieldlayout.cannot.hide.this.field", "'" + fieldLayoutItem.getOrderableField().getId() + "'"));
                }
            }

            // if this field layout is actually in use, then add a reindex message
            if (fieldLayoutSchemeHelper.doesChangingFieldLayoutRequireMessage(getRemoteUser(), getFieldLayout()))
            {
                reindexMessageManager.pushMessage(getRemoteUser(), "admin.notifications.task.field.configuration");
            }
        }
        return getFieldRedirect();
    }

    @RequiresXsrfCheck
    public String doRequire() throws Exception
    {
        doValidation();
        if (!invalidInput())
        {
            List fieldLayoutItems = getOrderedList();
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) fieldLayoutItems.get(getRequire().intValue());
            if (getFieldManager().isRequirableField(fieldLayoutItem.getOrderableField()))
            {
                if (fieldLayoutItem.isRequired())
                {
                    getFieldLayout().makeOptional(fieldLayoutItem);
                }
                else
                {
                    getFieldLayout().makeRequired(fieldLayoutItem);
                }
                store();
            }
            else
            {
                addErrorMessage(getText("admin.errors.fieldlayout.cannot.make.this.field.optional","'" + getText(fieldLayoutItem.getOrderableField().getNameKey()) + "'"));
            }
        }
        return getFieldRedirect();
    }

    protected abstract void store();

    public boolean isHideable(FieldLayoutItem fieldLayoutItem)
    {
        return ManagerFactory.getFieldManager().isHideableField(fieldLayoutItem.getOrderableField());
    }

    public boolean isRequirable(FieldLayoutItem fieldLayoutItem)
    {
        if (ManagerFactory.getFieldManager().isRequirableField(fieldLayoutItem.getOrderableField()))
        {
            return !fieldLayoutItem.isHidden();
        }
        else
        {
            return false;
        }
    }

    public boolean isMandatory(FieldLayoutItem fieldLayoutItem)
    {
        return ManagerFactory.getFieldManager().isMandatoryField(fieldLayoutItem.getOrderableField());
    }    

    public boolean isUnscreenable(FieldLayoutItem fieldLayoutItem)
    {
        return ManagerFactory.getFieldManager().isUnscreenableField(fieldLayoutItem.getOrderableField());
    }

    public Collection getFieldScreenTabs(OrderableField orderableField)
    {
        String fieldId = orderableField.getId();
        if (!fieldScreenTabMap.containsKey(fieldId))
        {
            fieldScreenTabMap.put(fieldId, fieldScreenManager.getFieldScreenTabs(orderableField.getId()));
        }

        return (Collection) fieldScreenTabMap.get(fieldId);
    }

    public boolean isCustomField(FieldLayoutItem fieldLayoutItem)
    {
        return ManagerFactory.getFieldManager().isCustomField(fieldLayoutItem.getOrderableField());
    }

    public boolean isHasDefaultFieldLayout()
    {
        try
        {
            return getFieldLayoutManager().hasDefaultFieldLayout();
        }
        catch (DataAccessException e)
        {
            log.error("Error determining whether the default layout is used.", e);
            addErrorMessage(getText("admin.errors.fieldlayout.error.determining.if.default.used"));
        }
        return false;
    }

    public String doRestoreDefaults() throws Exception
    {
        try
        {
            getFieldLayoutManager().restoreDefaultFieldLayout();
        }
        catch (DataAccessException e)
        {
            log.error("Error while restroring default field layout.", e);
            addErrorMessage(getText("admin.errors.fieldlayout.error.restoring.default"));
        }
        return getFieldRedirect();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getHide()
    {
        return hide;
    }

    public void setHide(Integer hide)
    {
        this.hide = hide;
    }

    public Integer getRequire()
    {
        return require;
    }

    public void setRequire(Integer require)
    {
        this.require = require;
    }

    public String getFieldName(Field field)
    {
        if (getFieldManager().isCustomField(field))
        {
            return field.getNameKey();
        }
        else
        {
            return getText(field.getNameKey());
        }
    }

    public RendererManager getRendererManager()
    {
        return rendererManager;
    }

    public String getRendererDisplayName(String rendererType)
    {
        final HackyRendererType hackyRendererType = HackyRendererType.fromKey(rendererType);
        if (hackyRendererType != null)
        {
            return getText(hackyRendererType.getDisplayNameI18nKey());
        }
        else
        {
            return rendererManager.getRendererForType(rendererType).getDescriptor().getName();
        }
    }

    protected FieldManager getFieldManager()
    {
        return ManagerFactory.getFieldManager();
    }

    protected FieldLayoutManager getFieldLayoutManager()
    {
        return getFieldManager().getFieldLayoutManager();
    }

    public boolean isRenderable(final OrderableField field)
    {
        if (field instanceof RenderableField)
        {
            RenderableField renderableField = (RenderableField) field;
            final boolean isRenderable = renderableField.isRenderable();
            //customfields all implement the RenderableField interface so if the field says it's not
            //renderable and it is a custom field we need to check if its renderers should be overriden
            if (!isRenderable && field instanceof CustomField)
            {
                return hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field);
            }
            else
            {
                return isRenderable;
            }
        }
        else
        {
            return hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field);
        }
    }
}

/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.constants;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.web.action.admin.statuses.ViewStatuses;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractViewConstants extends AbstractConstantAction
{
    private Map fields;
    String up;
    String down;
    String name;
    String description;
    String iconurl;
    String make;
    // New statuses are given ids starting from 10000 - avoids conflict with future system statuses.
    private static final Long NEW_STATUS_START_ID = new Long(10000);
    private final TranslationManager translationManager;

    protected AbstractViewConstants(TranslationManager translationManager)
    {
        this.translationManager = translationManager;
    }

    public String doAddConstant() throws Exception
    {
        // validate
        validateName();

        if (invalidInput())
            return ERROR;

        addConstant();

        return redirectToView();
    }

    protected void validateName()
    {
        if (!TextUtils.stringSet(name))
        {
            //NOTE-these translations mightn't work well in other languages :S
            addError("name", getText("admin.errors.must.specify.a.name.for.the.to.be.added",getNiceConstantName()));
            //addError("name", "You must specify a name for the " + getNiceConstantName() + " to be added.");
        }
        else
        {
            for (Iterator iterator = getConstants().iterator(); iterator.hasNext();)
            {
                GenericValue constant = (GenericValue) iterator.next();
                if (name.trim().equalsIgnoreCase(constant.getString("name")))
                {
                    addError("name", getText("admin.errors.constant.already.exists", getNiceConstantName()));
                    break;
                }
            }
        }
    }

    protected GenericValue addConstant() throws GenericEntityException
    {
        // create new constant
        // Ensure newly added statuses have ids that will not conflict with future system status ids
        // New user statuses will be created from id 10000 onwards
        String entityName = getConstantEntityName();
        if (TextUtils.stringSet(entityName))
        {
            if (entityName.equals(ViewStatuses.STATUS_ENTITY_NAME)
                && new Long(EntityUtils.getNextStringId(entityName)).longValue() < NEW_STATUS_START_ID.longValue())
            {
                addField("id", NEW_STATUS_START_ID.toString());
            }
            else
            {
                addField("id", EntityUtils.getNextStringId(entityName));
            }
        }
        else
            throw new IllegalArgumentException("Unable to create an entity without a valid name.");

        // populate the rest of the fields to create the new entity
        addField("name", name);
        addField("description", description);
        addField("iconurl", iconurl);
        addField("sequence", new Long(getMaxSequenceNo() + 1));

        GenericValue createdValue = EntityUtils.createValue(entityName, getFields());

        // reset data
        name = null;
        description = null;
        
        clearCaches();

        return createdValue;
    }

    protected abstract String redirectToView();

    private long getMaxSequenceNo()
    {
        Collection constants = getConstants();
        long maxSequence = 0;
        for (Iterator iterator = constants.iterator(); iterator.hasNext();)
        {
            GenericValue constantGV = (GenericValue) iterator.next();
            long thisSequence = constantGV.getLong("sequence").longValue();
            if (thisSequence > maxSequence)
                maxSequence = thisSequence;
        }
        return maxSequence;
    }

    public String doMoveUp() throws Exception
    {
        List reordered = new ArrayList();

        for (Iterator iterator = getConstants().iterator(); iterator.hasNext();)
        {
            GenericValue value = (GenericValue) iterator.next();

            if (value.getString("id").equals(up) && reordered.size() == 0)
                return SUCCESS;
            else if (value.getString("id").equals(up))
                reordered.add(reordered.size() - 1, value);
            else
                reordered.add(value);
        }

        storeAndClearCaches(reordered);
        return getResult();
    }

    public String doMoveDown() throws Exception
    {
        List reordered = new ArrayList();

        for (Iterator iterator = getConstants().iterator(); iterator.hasNext();)
        {
            GenericValue value = (GenericValue) iterator.next();

            if (value.getString("id").equals(down) && !iterator.hasNext())
                return SUCCESS;
            else if (value.getString("id").equals(down))
            {
                reordered.add(iterator.next());
                reordered.add(value);
            }
            else
                reordered.add(value);
        }

        storeAndClearCaches(reordered);
        return getResult();
    }

    public String doMakeDefault() throws Exception
    {
        getApplicationProperties().setString(getDefaultPropertyName(), make);
        return SUCCESS;
    }

    /**
     * Store reordered priorities, and then clear the caches
     */
    private void storeAndClearCaches(List reordered) throws GenericEntityException
    {
        for (int i = 0; i < reordered.size(); i++)
        {
            GenericValue value = (GenericValue) reordered.get(i);
            value.set("sequence", new Long(i + 1));
        }

        getDelegator().storeAll(reordered);

        clearCaches();
    }

    protected void addField(String key, Object value)
    {
        if (getFields() == null)
        {
            fields = new HashMap();
        }
        getFields().put(key, value);
    }

    private Map getFields()
    {
        return fields;
    }

    public boolean isDefault(GenericValue constant)
    {
        String constantId = getApplicationProperties().getString(getDefaultPropertyName());
        return (constantId != null && constant.getString("id").equals(constantId));
    }

    protected abstract String getDefaultPropertyName();

    public void setUp(String up)
    {
        this.up = up;
    }

    public void setDown(String down)
    {
        this.down = down;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getIconurl()
    {
        return iconurl;
    }

    public void setIconurl(String iconurl)
    {
        this.iconurl = iconurl;
    }

    public void setMake(String make)
    {
        this.make = make;
    }

    public boolean isTranslatable()
    {
        //JRA-16912: Only show the 'Translate' link if there's any installed languages to translate to!
        return !translationManager.getInstalledLocales().isEmpty();
    }
}

/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.admin;

import com.atlassian.jira.action.JiraNonWebActionSupport;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @deprecated Use {@link com.atlassian.jira.event.ListenerManager#deleteListener(Class)} instead. Since v5.0.
 */
public class ListenerDelete extends JiraNonWebActionSupport
{
    String clazz;

    @Override
    protected String doExecute() throws Exception
    {
        try
        {
            if (clazz != null)
            {
                final Collection<GenericValue> listenerConfigs = getDelegator().findAll("ListenerConfig");
                final List<GenericValue> toRemove = new ArrayList<GenericValue>();

                for (final GenericValue listenerConfig : listenerConfigs)
                {
                    if (listenerConfig.getString("clazz").equals(clazz))
                    {
                        toRemove.add(listenerConfig);
                    }
                }

                getDelegator().removeAll(toRemove);
            }
        }
        catch (final GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.listenerdelete.error.removing.listeners", e.toString()));
        }

        return getResult();
    }

    public String getClazz()
    {
        return clazz;
    }

    public void setClazz(final String clazz)
    {
        this.clazz = clazz;
    }
}

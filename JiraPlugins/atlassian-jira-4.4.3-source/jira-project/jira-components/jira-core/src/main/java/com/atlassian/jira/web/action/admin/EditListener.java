/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.event.JiraListener;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ParameterAware;

import java.util.Map;

@WebSudoRequired
public class EditListener extends JiraWebActionSupport implements ParameterAware
{
    Long id;
    GenericValue listener;
    Map params;
    private final PluginAccessor pluginAccessor;

    public EditListener(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            PropertySet ps = OFBizPropertyUtils.getPropertySet(getListener());

            // create a dummy listener
            final JiraListener listener = getJiraListener();

            // set all the possible accepted parameters - blank or missing params are set to null
            for (int i = 0; i < listener.getAcceptedParams().length; i++)
            {
                String paramName = listener.getAcceptedParams()[i];

                String paramValue = ((String[]) params.get(paramName))[0];
                if (!TextUtils.stringSet(paramValue))
                {
                    paramValue = null;
                }

                if (paramValue != null)
                    ps.setString(paramName, paramValue);
                else if (ps.exists(paramName))
                    ps.remove(paramName);
            }

            // now update the listeners loaded
            ManagerFactory.getListenerManager().refresh();
        }
        catch (Exception e)
        {
            log.error("Error occurred trying to update listener properties: "+e, e);
            addErrorMessage(getText("admin.errors.updating.listener.properties") + " " + e);
        }

        if (getHasErrorMessages())
            return ERROR;
        else
            return getRedirect("ViewListeners!default.jspa");
    }

    protected void doValidation()
    {
        if (getListener() == null)
        {
            addErrorMessage(getText("admin.errors.listener.does.not.exist"));
        }

        super.doValidation();
    }

    public String[] getAcceptedParams()
    {
        try
        {
            return getJiraListener().getAcceptedParams();
        }
        catch (Exception e)
        {
            log.error("Error getting accepted params: " + e.getMessage(), e);
            return new String[0];
        }
    }

    public JiraListener getJiraListener() throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return (JiraListener) ClassLoaderUtils.loadClass(getListener().getString("clazz"), pluginAccessor.getClassLoader()).newInstance();
    }

    public String getParamValue(String s)
    {
        PropertySet ps = OFBizPropertyUtils.getPropertySet(getListener());

        return ps.getString(s);
    }

    public GenericValue getListener()
    {
        if (listener == null)
        {
            try
            {
                listener = getDelegator().findByPrimaryKey("ListenerConfig", EasyMap.build("id", id));
            }
            catch (GenericEntityException e)
            {
                log.error("Error getting ListenerConfig with id "+id, e);
            }
        }

        return listener;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public void setParameters(Map map)
    {
        this.params = map;
    }
}

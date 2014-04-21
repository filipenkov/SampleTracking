/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.event.JiraListener;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import java.util.Collection;
import java.util.Iterator;

@WebSudoRequired
public class ViewListeners extends JiraWebActionSupport
{
    Collection listeners;
    Long delete;
    String name;
    String clazz;
    private static final Logger LOG = Logger.getLogger(ViewListeners.class);
    private final ComponentClassManager componentClassManager;

    public ViewListeners(final ComponentClassManager componentClassManager)
    {
        this.componentClassManager = componentClassManager;
    }

    public void setDelete(Long delete)
    {
        this.delete = delete;
    }

    protected void doValidation()
    {
        if (delete == null)
        {
            //only do validation if we are not deleting.
            if (name == null || "".equals(name.trim()))
            {
                addError("name", getText("admin.errors.specify.name.for.listener"));
            }

            if (clazz == null || "".equals(clazz.trim()))
            {
                addError("clazz", getText("admin.errors.specify.class.for.listener"));
            }

            boolean listenerExists = false;

            //check that no listener exists with the same name.
            for (Iterator iterator = getListeners().iterator(); iterator.hasNext();)
            {
                GenericValue listener = (GenericValue) iterator.next();
                if (name.equalsIgnoreCase(listener.getString("name")))
                {
                    addError("name", getText("admin.errors.listener.already.exists", name));
                }
                if (clazz.equals(listener.getString("clazz")))
                {
                    listenerExists = true;
                }
            }

            //don't lookup the classname unless there are no errors with the above
            if (!getHasErrors())
            {
                try
                {
                    JiraListener listener = componentClassManager.newInstance(clazz);

                    //if another listener exists with the same class, then check if uniqueness should be enforced
                    if (listenerExists && listener.isUnique())
                    {
                        addError("clazz", getText("admin.errors.cannot.add.listener"));
                    }
                }
                catch (ClassNotFoundException ex)
                {
                    addError("clazz", getText("admin.errors.class.not.found", clazz));
                    log.debug("User tried to add listener via the admin UI. The specified class [" + clazz + "] was not found when trying to add the listener", ex);
                }
                catch (ClassCastException e)
                {
                    addError("clazz", getText("admin.errors.class.is.not.listener", clazz));
                    log.debug("User tried to add listener via the admin UI. The specified class [" + clazz + "] is not of type JiraListener", e);
                }
                catch (Exception e)
                {
                    addError("clazz", getText("admin.errors.exception.loading.class") + " [" + e.getMessage() + "].");
                    log.error("User tried to add listener via the admin UI. Exception loading the specified class: [" + e.getMessage() + "]", e);
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (name != null && clazz != null)
        {
            try
            {
                ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.LISTENER_CREATE, EasyMap.build("name", getName(), "clazz", getClazz()));
                ActionUtils.checkForErrors(aResult);

                name = null;
                clazz = null;
                listeners = null;
            }
            catch (Exception e)
            {
                addErrorMessage("Error adding listener: " + e.toString() + ".");
            }
        }

        return getRedirect("ViewListeners!default.jspa");
    }

    @RequiresXsrfCheck
    public String doDelete() throws GenericEntityException
    {
        getDelegator().removeByAnd("ListenerConfig", EasyMap.build("id", delete));
        ManagerFactory.getListenerManager().refresh();
        return getRedirect("ViewListeners!default.jspa");
    }

    /**
     * Get all the listeners in the system.
     * @return A collection of GenericValues representing listeners
     */
    public Collection getListeners()
    {
        if (listeners == null)
        {
            try
            {
                listeners = getDelegator().findAll("ListenerConfig");
            }
            catch (GenericEntityException e)
            {
                LOG.error("GenericEntityException", e);
            }
        }

        return listeners;
    }

    public JiraListener getListenerImplementation(GenericValue listener)
    {
        try
        {
            return componentClassManager.newInstance(listener.getString("clazz"));
        }
        catch (Throwable e)
        {
            log.warn("Exception getting listener info " + e.getMessage(), e);
            return null;
        }
    }

    public boolean isListenerDeletable(GenericValue listener)
    {
        JiraListener listenerImp = getListenerImplementation(listener);
        if (listenerImp != null)
        {
            return !listenerImp.isInternal();
        }
        else
        {
            return true;
        }
    }

    public boolean isListenerEditable(GenericValue listener)
    {
        JiraListener listenerImp = getListenerImplementation(listener);
        if (listenerImp != null)
        {
            return listenerImp.getAcceptedParams().length > 0;
        }
        else
        {
            return true;
        }
    }

    public void setName(String name)
    {
        this.name = name.trim();
    }

    public void setClazz(String clazz)
    {
        this.clazz = clazz.trim();
    }

    public String getName()
    {
        return name;
    }

    public String getClazz()
    {
        return clazz;
    }
}

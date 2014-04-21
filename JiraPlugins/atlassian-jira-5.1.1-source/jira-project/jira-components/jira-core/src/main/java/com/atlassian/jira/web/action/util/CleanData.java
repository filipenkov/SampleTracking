/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.util;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import webwork.action.ServletActionContext;

import java.util.Iterator;

public class CleanData extends JiraWebActionSupport
{
    private String userName;
    private String password;
    private boolean dataCleaned;
    private final CrowdService crowdService;

    public CleanData(CrowdService crowdService)
    {
        this.crowdService = crowdService;
    }

    public String doDefault() throws Exception
    {
        dataCleaned = false;
        return SUCCESS;
    }

    protected void doValidation()
    {
        //check that the user is an admin and that the password is correct
        User user = crowdService.getUser(userName);
        if (user != null)
        {
            try
            {
                crowdService.authenticate(user.getName(), password);
            }
            catch (FailedAuthenticationException e)
            {
                addErrorMessage(getText("admin.errors.cleandata.username.password.incorrect"));
                return;
            }

            if (!nonAdminUpgradeAllowed())
            {
                boolean hasAdminPermission = ManagerFactory.getPermissionManager().hasPermission(Permissions.ADMINISTER, user);

                if (!hasAdminPermission)
                {
                    addError("userName", getText("admin.errors.cleandata.no.admin.permission"));
                }
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.cleandata.username.password.incorrect"));
        }
    }

    protected String doExecute() throws Exception
    {
        OfBizDelegator ofBizDelegator = ComponentManager.getComponent(OfBizDelegator.class);
        ApplicationProperties applicationProperties = ComponentManager.getComponent(ApplicationProperties.class);
        DataCleaner dataCleaner = new DataCleaner(applicationProperties, ofBizDelegator);
        dataCleaner.clean();

        // Lock JIRA so the server has to be restarted and
        JohnsonEventContainer cont = JohnsonEventContainer.get(ServletActionContext.getServletContext());
        for (Iterator iterator = cont.getEvents().iterator(); iterator.hasNext();)
        {
            Event event = (Event) iterator.next();
            if (event != null && event.getKey().equals(EventType.get("export-illegal-xml")))
            {
                cont.removeEvent(event);
            }
        }

        Event newEvent = new Event(EventType.get("restart"), "The illegal XML characters have been removed. The server needs to be restarted.", EventLevel.get(EventLevel.ERROR));
        cont.addEvent(newEvent);

        setDataCleaned(true);

        return getResult();
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    private boolean nonAdminUpgradeAllowed()
    {
        return Boolean.valueOf(System.getProperty(SystemPropertyKeys.UPGRADE_SYSTEM_PROPERTY)).booleanValue();
    }

    public boolean isDataCleaned()
    {
        return dataCleaned;
    }

    private void setDataCleaned(boolean dataCleaned)
    {
        this.dataCleaned = dataCleaned;
    }
}

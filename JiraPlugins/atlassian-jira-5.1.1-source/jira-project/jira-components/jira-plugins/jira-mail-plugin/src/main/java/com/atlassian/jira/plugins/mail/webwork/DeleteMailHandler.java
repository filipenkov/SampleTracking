/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;


import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

@WebSudoRequired
public class DeleteMailHandler extends MailWebActionSupport
{
    private final ServiceManager serviceManager;

    private Long id;
    private boolean confirmed;

    public DeleteMailHandler(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    protected void doValidation()
    {
        if (getId() == null || !isConfirmed())
        {
            addErrorMessage(getText("admin.errors.mail.confirm.deletion.of.server"));
        }
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }


    @RequiresXsrfCheck
    public String doExecute() throws Exception
    {
        if (!canDeleteService(id))
        {
            return "securitybreach";
        }

        log.debug("Removing Service with id " + id);
        serviceManager.removeService(id);

        return getRedirect("IncomingMailServers.jspa");
    }

    private boolean canDeleteService(final Long serviceId) throws Exception
    {
        return Iterables.any(serviceManager.getServicesManageableBy(getLoggedInUser()), new Predicate<JiraServiceContainer>()
        {
            @Override
            public boolean apply(@Nullable JiraServiceContainer aServiceManageableByTheUser)
            {
                return serviceId.equals(aServiceManageableByTheUser.getId());
            }
        });
    }

    public Long getId()
    {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(Long id)
    {
        this.id = id;
    }

    public String getCancelURI()
    {
        return ViewMailServers.INCOMING_MAIL_ACTION;
    }

    public String getName() {
        try
        {
            JiraServiceContainer service = serviceManager.getServiceWithId(id);
            return service != null ? service.getName() : "";
        }
        catch (Exception e)
        {
            return "";
        }
    }
}


/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugins.mail.ServiceConfiguration;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.ServiceTypes;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.HelpUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.velocity.tools.generic.SortTool;
import webwork.action.ActionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MailWebActionSupport extends JiraWebActionSupport
{
    public SortTool getSorter() {
		return new SortTool();
	}

    protected boolean canAddService(final String clazz)
    {
        return getServiceTypes().isManageableBy(getLoggedInUser(), clazz);
    }

    @Nonnull
    protected ServiceTypes getServiceTypes() {
        return ComponentManager.getComponentInstanceOfType(ServiceTypes.class);
    }

    public boolean canPerformAjaxSearch() {
        return getComponentInstanceOfType(UserPickerSearchService.class).canPerformAjaxSearch(getLoggedInUser());
    }

    public BrowserUtils getBrowserUtils() {
        return new BrowserUtils();
    }

    @Nullable
    public static ServiceConfiguration getConfiguration() {
        try {
            return (ServiceConfiguration) ActionContext.getSession().get(ServiceConfiguration.ID);
        } catch (ClassCastException e) {
            // ignore, can happen when plugin was re-loaded
        }
        return null;
    }

    @Nullable
    public static void setConfiguration(ServiceConfiguration configuration) {
        ActionContext.getSession().put(ServiceConfiguration.ID, configuration);
    }

    @Nonnull
    protected ServiceManager getServiceManager() {
        return ComponentAccessor.getServiceManager();
    }

    @Nullable
    public JiraServiceContainer getService(Long id)
    {
        final JiraServiceContainer service;
        try
        {
            service = getServiceManager().getServiceWithId(id);
        }
        catch (Exception e)
        {
            log.error(String.format("Unabled to get service with id %d", id), e);
            return null;
        }

        // JRADEV-9039: don't compare by name only, otherwise extending these services is not possible
        for (Class cls : ImmutableSet.of(MailFetcherService.class, FileService.class))
        {
            if (cls.isAssignableFrom(service.getServiceClassObject()))
            {
                return service;
            }
        }

        return null;
    }

    protected boolean canEditService(final Long serviceId) throws Exception
    {
        return Iterables.any(getServiceManager().getServicesManageableBy(getLoggedInUser()), new Predicate<JiraServiceContainer>()
        {
            @Override
            public boolean apply(@Nullable JiraServiceContainer aServiceManageableByTheUser)
            {
                return serviceId.equals(aServiceManageableByTheUser != null ? aServiceManageableByTheUser.getId() : null);
            }
        });
    }

    public HelpUtil.HelpPath getHelpPath(String key) {
        return new HelpUtil().getHelpPath(key);
    }
}

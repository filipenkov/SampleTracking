/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Map;

@WebSudoRequired
public class EditHandlerDetailsUsingParamsWebAction extends AbstractEditHandlerDetailsWebAction
{
    private String forwardEmail;
    private String params;

    public EditHandlerDetailsUsingParamsWebAction(PluginAccessor pluginAccessor)
    {
        super(pluginAccessor);
    }

    @Override
    protected void doValidation()
    {
        if (configuration == null)
        {
            return; // short-circuit in case we lost session, goes directly to doExecute which redirects user
        }

        super.doValidation();

        if (StringUtils.isNotBlank(getForwardEmail()) && !TextUtils.verifyEmail(TextUtils.noNull(getForwardEmail()).trim()))
        {
            addError("forwardEmail", getText("admin.errors.invalid.email"));
        }
    }

    @Override
    protected void copyServiceSettings(JiraServiceContainer serviceContainer) throws ObjectConfigurationException
    {
        forwardEmail = serviceContainer.getProperty("forwardEmail");
        params = serviceContainer.getProperty("handler.params");
    }

    @Override
    protected Map<String, String[]> getAdditionalServiceParams() throws Exception
    {
        return MapBuilder.<String, String[]>newBuilder(MailFetcherService.FORWARD_EMAIL, new String[] { getForwardEmail() })
                .toMutableMap();
    }

    @Override
    protected Map<String, String[]> getServiceParams() throws Exception
    {
        final Map<String, String[]> serviceParams = super.getServiceParams();
        serviceParams.put(AbstractMessageHandlingService.KEY_HANDLER_PARAMS, new String[] { getParams() });
        return serviceParams;
    }

    public String getForwardEmail()
    {
        return forwardEmail;
    }

    @SuppressWarnings ("unused")
    public void setForwardEmail(String forwardEmail)
    {
        this.forwardEmail = forwardEmail;
    }

    @Override
    protected Map<String, String> getHandlerParams()
    {
        return Collections.emptyMap();
    }

    public String getParams()
    {
        return params;
    }

    @SuppressWarnings ("unused")
    public void setParams(String params)
    {
        this.params = params;
    }
}

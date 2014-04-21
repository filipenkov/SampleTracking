/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.rest;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugins.mail.DryRunMessageHandlerExecutionMonitor;
import com.atlassian.jira.plugins.mail.HandlerDetailsValidator;
import com.atlassian.jira.plugins.mail.ServiceConfiguration;
import com.atlassian.jira.plugins.mail.model.HandlerDetailsModel;
import com.atlassian.jira.plugins.mail.model.TestResultModel;
import com.atlassian.jira.plugins.mail.model.ValidationResultModel;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.plugins.mail.handlers.DryRunMessageHandlerContext;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.service.util.handler.MessageHandlerStats;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path ("/message-handlers")
public class MessageHandlersResource
{
    protected static final Logger logger = Logger.getLogger(MessageHandlersResource.class);

    private final JiraAuthenticationContext authenticationContext;
    private final GlobalPermissionManager globalPermissionManager;
    private final HandlerDetailsValidator detailsValidator;
    private final PluginAccessor pluginAccessor;

    public MessageHandlersResource(JiraAuthenticationContext authenticationContext,
            GlobalPermissionManager globalPermissionManager,
            HandlerDetailsValidator detailsValidator,
            PluginAccessor pluginAccessor) {
		this.authenticationContext = authenticationContext;
		this.globalPermissionManager = globalPermissionManager;
        this.detailsValidator = detailsValidator;
        this.pluginAccessor = pluginAccessor;
    }

    protected boolean isAdministrator() {
        User currentUser = authenticationContext.getLoggedInUser();
        return (currentUser != null) && globalPermissionManager.hasPermission(Permissions.ADMINISTER, currentUser);
    }

    @POST
    @Path("/validate")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes (MediaType.APPLICATION_FORM_URLENCODED)
    public Response validate(@Context HttpServletRequest req, @FormParam("detailsJson") String detailsJson) {
        if (!isAdministrator()) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

        final ServiceConfiguration configuration = getConfiguration(req);
        if (configuration == null) {
            return Response.status(Response.Status.GONE).build();
        }

        try
        {
            final HandlerDetailsModel details = new ObjectMapper().readValue(detailsJson, HandlerDetailsModel.class);
            final ErrorCollection validation = detailsValidator.validateDetails(details);
            return Response.ok(new ValidationResultModel(validation)).build();
        }
        catch (Exception e)
        {
            logger.error("Unable to validate", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path ("/test")
    @Produces ({ MediaType.APPLICATION_JSON })
    @Consumes (MediaType.APPLICATION_FORM_URLENCODED)
    public Response testHandler(@Context HttpServletRequest req, @FormParam ("detailsJson") String detailsJson)
    {
        if (!isAdministrator())
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        final ServiceConfiguration configuration = getConfiguration(req);
        if (configuration == null)
        {
            return Response.status(Response.Status.GONE).build();
        }

        try
        {
            final AbstractMessageHandlingService service = getComponentClassManager().newInstance(configuration.getServiceClass());
            if (service != null)
            {
                final Map<String, String> serviceParams = Maps.newHashMap(configuration.toMap(pluginAccessor));

                final HandlerDetailsModel details = new ObjectMapper().readValue(detailsJson, HandlerDetailsModel.class);
                final ErrorCollection validation = detailsValidator.validateDetails(details);
                if (validation.hasAnyErrors())
                {
                    return Response.ok(new ValidationResultModel(validation)).build();
                }

                if (StringUtils.isNotBlank(details.getForwardEmail()))
                {
                    serviceParams.put(MailFetcherService.FORWARD_EMAIL, details.getForwardEmail());
                }

                serviceParams.put(AbstractMessageHandlingService.KEY_HANDLER_PARAMS, ServiceUtils.toParameterString(details.toServiceParams()));

                final MapPropertySet set = new MapPropertySet();
                set.setMap(serviceParams);
                final DryRunMessageHandlerExecutionMonitor executionMonitor = new DryRunMessageHandlerExecutionMonitor();
                final DryRunMessageHandlerContext context = new DryRunMessageHandlerContext(executionMonitor);

                // we need this trick, otherwise javamail won't find data handler (activation library) for message content
                // and message.getContent() will be InputStream :(
                ContextClassLoaderSwitchingUtil.runInContext(service.getClass().getClassLoader(), new Runnable()
                {
                    @Override
                    public void run()
                    {
                        service.setContext(context);
                        try
                        {
                            service.init(set);
                        }
                        catch (ObjectConfigurationException e)
                        {
                            throw new RuntimeException(e);
                        }
                        service.run();
                    }
                });

                return Response.ok(new TestResultModel(!executionMonitor.hasErrors(), ImmutableList.copyOf(executionMonitor.getErrorMessages()),
                        new MessageHandlerStats(executionMonitor.getNumMessages(), context.getNumCreatedIssues(),
                                context.getNumCreatedUsers(),
                                context.getNumCreatedComments(), context.getNumCreatedAttachments(),
                                executionMonitor.getNumMessagesRejected()),
                        executionMonitor.getAllMessages())).build();
            }
            else
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch (Exception e)
        {
            logger.error("Unable to execute test", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Nullable
    private ServiceConfiguration getConfiguration(HttpServletRequest req)
    {
        HttpSession session = req.getSession(false);
        if (session != null) {
            try {
                return (ServiceConfiguration) session.getAttribute(ServiceConfiguration.ID);
            } catch(ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    @Nonnull
    protected ComponentClassManager getComponentClassManager()
    {
        return ComponentManager.getComponentInstanceOfType(ComponentClassManager.class);
    }
}

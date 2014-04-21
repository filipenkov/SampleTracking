package com.atlassian.jira.dev.backdoor;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Use this backdoor to manipulate Services as part of setup for tests.
 * <p/>
 * This class should only be called by the {@link com.atlassian.jira.functest.framework.backdoor.ServicesControl}.
 *
 * @since v5.0
 */
@Path ("services")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ServicesBackdoor
{
    private final ServiceManager serviceManager;

    public ServicesBackdoor(ServiceManager serviceManager)
    {
        this.serviceManager = serviceManager;
    }

    @GET
    public Response getServices() throws Exception
    {
        final List<ServiceBean> services = Lists.newArrayList();
        for (JiraServiceContainer serviceContainer : serviceManager.getServices())
        {
            services.add(new ServiceBean(serviceContainer));
        }
        return Response.ok(services).build();
    }

    @GET
    @Path ("/{id}")
    public Response getService(@PathParam ("id") Long id) throws Exception
    {
        final JiraServiceContainer container = serviceManager.getServiceWithId(id);
        return container == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(new ServiceBean(container)).build();
    }

    public static class ServiceBean
    {
        public Long id;
        public String name;
        public String serviceClass;
        public boolean usable;
        public Map<String, String> params;

        public ServiceBean()
        {
        }

        public ServiceBean(JiraServiceContainer serviceContainer)
        {
            id = serviceContainer.getId();
            name = serviceContainer.getName();
            serviceClass = serviceContainer.getServiceClass();
            usable = serviceContainer.isUsable();
            try
            {
                if (usable && serviceContainer.getProperties() != null)
                {
                    params = Maps.newHashMap();
                    for (Object o : serviceContainer.getProperties().getKeys())
                    {
                        String key = (String) o;
                        params.put(key, serviceContainer.getProperty(key));
                    }
                }
            }
            catch (ObjectConfigurationException e)
            {
                // ignore
            }
        }
    }
}

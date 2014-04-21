package com.atlassian.jira.web.action.func.service;

import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.ServiceManager.ServiceScheduleSkipper;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.quartz.Scheduler;

import java.util.Collection;

@WebSudoRequired
public class ServiceExecutor extends JiraWebActionSupport
{
    private final ServiceManager manager;
    private final Scheduler scheduler;

    public ServiceExecutor(final ServiceManager manager, final Scheduler scheduler)
    {
        this.manager = manager;
        this.scheduler = scheduler;
    }

    private long serviceId = 0;

    @Override
    protected void doValidation()
    {
        if (serviceId > 0)
        {
            if (!manager.containsServiceWithId(serviceId))
            {
                addError("serviceId", "No service with this id exists");
            }
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (serviceId > 0)
        {
            final ServiceScheduleSkipper skipper = manager.getScheduleSkipper();
            skipper.addService(serviceId);
            scheduler.triggerJobWithVolatileTrigger("ServicesJob", Scheduler.DEFAULT_GROUP);
            skipper.awaitServiceRun(serviceId);
        }
        return super.doExecute();
    }

    public long getServiceId()
    {
        return serviceId;
    }

    public Collection<JiraServiceContainer> getServices()
    {
        return manager.getServices();
    }

    public void setServiceId(final long serviceId)
    {
        this.serviceId = serviceId;
    }
}

package com.atlassian.sal.jira.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.configurable.ObjectConfigurationException;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.sal.api.scheduling.PluginScheduler;

public class JiraPluginScheduler implements PluginScheduler
{
    private static final Logger log = Logger.getLogger(JiraPluginScheduler.class);

    public static final String PLUGIN_JOB_NAME = "pluginJobName";
    public static final String REPEAT_INTERVAL = "repeatInterval";
    public static final String INITIALLY_FIRED = "initiallyFired";

    private static final int SAFETY_FACTOR_MILLIS = 20;

    private final Map<String, JiraPluginSchedulerServiceDescriptor> serviceMap;
    private final ServiceManager serviceManager;

    public JiraPluginScheduler(final ServiceManager serviceManager)
    {
        serviceMap = Collections.synchronizedMap(new HashMap<String, JiraPluginSchedulerServiceDescriptor>());
        this.serviceManager = serviceManager;
    }

    Class<? extends JiraPluginSchedulerService> getSchedulerServiceClass()
    {
        return JiraPluginSchedulerService.class;
    }

    public void scheduleJob(final String name, final Class<? extends PluginJob> job, final Map<String, Object> jobDataMap,
                            final Date startTime, final long repeatInterval)
    {
        if (repeatInterval <= 0)
        {
            throw new IllegalArgumentException("repeatInterval must be greater than zero. current=" + repeatInterval);
        }

        // Create a map to hold the configuration for the job
        final Map<String, String[]> serviceDataMap = new HashMap<String, String[]>();
        serviceDataMap.put(PLUGIN_JOB_NAME, new String[] { name });
        serviceDataMap.put(REPEAT_INTERVAL, new String[] { Long.toString(repeatInterval) });
        serviceDataMap.put(INITIALLY_FIRED, new String[] { Boolean.FALSE.toString() });

        // This is the initial delay before we execute the job for the first time.
        // Since the underlying serviceManager only allows scheduling by intervals, it is a workaround
        // to get scheduling at exact time working.
        final long initialDelay = startTime.getTime() - System.currentTimeMillis();

        if (log.isDebugEnabled())
        {
            log.debug("start time =" + startTime.getTime() + " current time = " + System.currentTimeMillis()
                      + " thus the initialDelay =" + initialDelay);
        }

        // set up the service descriptor.
        final JiraPluginSchedulerServiceDescriptor sd = new JiraPluginSchedulerServiceDescriptor();
        sd.setJob(job);
        sd.setJobDataMap(jobDataMap);

        // Put a service descriptor in the map. This has to be before the service registration
        // to avoid a race condition if the service get kicked off so quickly.
        serviceMap.put(name, sd);

        try
        {
            // Remove the service if it exists.
            if (serviceManager.getServiceWithName(name) != null)
            {
                removeServicesByName(name);
            }

            // since there might be some delay between the time this method is called and the initialDelay variable
            // is calculated, SAFETY_FACTOR_MILLIS is introduced to allow the job to still be executed.
            if (initialDelay > SAFETY_FACTOR_MILLIS)
            {
                final JiraServiceContainer serviceContainer =
                        serviceManager.addService(name, getSchedulerServiceClass(), initialDelay, serviceDataMap);
                //this fixes a problem with the isDueAt calculation which consists of lastRun + delay <= currentTime.
                //Setting lastRun here means that a task will only kick of after the specified startTime!
                serviceContainer.setLastRun();

            }
            else
            {
                serviceManager.addService(name, getSchedulerServiceClass(), SAFETY_FACTOR_MILLIS, serviceDataMap);
            }

        }
        catch (final Exception e)
        {
            // this entry now has no use.
            serviceMap.remove(name);
            throw new RuntimeException("Error scheduling service", e);
        }
    }

    JiraPluginSchedulerServiceDescriptor getServiceDescriptor(final String name)
    {
        return serviceMap.get(name);
    }

    public void unscheduleJob(final String name)
    {
        removeServicesByName(name);
    }

    /**
     * Reconfigure the job by:-
     *
     * - set the repeat interval to the one stored under {@link #REPEAT_INTERVAL} key.
     * - set {@link #INITIALLY_FIRED} to true.
     *
     * @param name jobName.
     */
    protected void reconfigureAfterFirstFire(final String name)
    {
        boolean found = false;

        final Collection<JiraServiceContainer> services = serviceManager.getServices();
        for (final JiraServiceContainer service : services)
        {
            if (name.equals(service.getName()))
            {
                found = true;
                PropertySet props;
                try
                {
                    props = service.getProperties();
                }
                catch (ObjectConfigurationException oce)
                {
                    throw new RuntimeException(oce);
                }

                long newRepeatInterval = resolveServiceRepeatInterval(service, props);

                try
                {
                    serviceManager.editServiceByName(name, newRepeatInterval, createdInitiallyFiredServiceDataMap(props));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(String.format("PluginScheduler job '%s' cannot alter its repeatInterval", name), e);
                }
            }
        }

        if (!found)
        {
            throw new IllegalStateException(String.format("PluginScheduler job '%s' is expected to exist but it doesn't", name));
        }
    }

    private long resolveServiceRepeatInterval(JiraServiceContainer serviceContainer, PropertySet props)
    {
        String repeatIntervalString = props.getString(REPEAT_INTERVAL);

        long repeatInterval;
        if (repeatIntervalString != null)
        {
            try
            {
                repeatInterval = Long.parseLong(repeatIntervalString);
            }
            catch (NumberFormatException e)
            {
                log.info(String.format("Could not parse number from repeat interval '%s' for service '%s'. "
                        + "Using delay value.", repeatIntervalString, serviceContainer.getName()));
                repeatInterval = serviceContainer.getDelay();
            }
        }
        else
        {
            // Handle legacy services that do not have an explicit repeatInterval value.
            log.info(String.format("Service '%s' has no repeat interval. Using delay value.",serviceContainer.getName()));
            repeatInterval = serviceContainer.getDelay();
        }

        return repeatInterval;
    }

    protected void removeServicesByName(final String name)
    {
        // We use getServices() rather than getServiceWithName() because there was
        // a bug where multiple services with the same name were being created.  getServiceWithName() will throw an
        // exception in that circumstance, so we'll just iterate through them all and delete all the ones that have
        // a matching name.
        final Collection<JiraServiceContainer> servicesToRemove = new ArrayList<JiraServiceContainer>();
        final Collection<JiraServiceContainer> services = serviceManager.getServices();
        boolean found = false;
        for (final JiraServiceContainer service : services)
        {
            if (name.equals(service.getName()))
            {
                servicesToRemove.add(service);
                found = true;
            }
        }

        if (!found)
        {
            throw new IllegalArgumentException(String.format("Cannot unschedule job '%s' because it does not exist.", name));
        }

        // remove services
        for (final JiraServiceContainer service : servicesToRemove)
        {
            try
            {
                serviceManager.removeService(service.getId());
            }
            catch (final Exception e)
            {
                log.error("Error removing service "+service.getName()+" ("+service.getId()+") from jira", e);
            }
        }
    }

    private static Map<String, String[]> createdInitiallyFiredServiceDataMap(final PropertySet propertySet)
    {
        final Map<String, String[]> serviceDataMap = new HashMap<String, String[]>();

        serviceDataMap.put(PLUGIN_JOB_NAME, new String[] { propertySet.getString(PLUGIN_JOB_NAME) });
        serviceDataMap.put(REPEAT_INTERVAL, new String[] { propertySet.getString(REPEAT_INTERVAL) });
        serviceDataMap.put(INITIALLY_FIRED, new String[] { Boolean.TRUE.toString() });

        return serviceDataMap;
    }
}

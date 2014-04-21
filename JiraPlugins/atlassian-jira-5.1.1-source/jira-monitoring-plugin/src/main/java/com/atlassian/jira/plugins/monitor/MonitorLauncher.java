package com.atlassian.jira.plugins.monitor;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.FeatureDisabledEvent;
import com.atlassian.jira.config.FeatureEnabledEvent;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.ListIterator;

import static com.atlassian.jira.config.CoreFeatures.ON_DEMAND;

/**
 * This class is used to start (or not) all JIRA monitoring.
 *
 * @since v5.1
 */
public class MonitorLauncher implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(MonitorLauncher.class);
    private static final String PLUGIN_KEY = "com.atlassian.jira.jira-monitoring-plugin";

    private final EventPublisher eventPublisher;
    private final MonitoringFeature monitoringFeature;
    private final List<MonitorService> monitorServices;
    private volatile State state = new Disabled();

    public MonitorLauncher(EventPublisher eventPublisher, MonitoringFeature monitoringFeature, List<MonitorService> monitorServices)
    {
        this.eventPublisher = eventPublisher;
        this.monitoringFeature = monitoringFeature;
        this.monitorServices = monitorServices;
    }

    @Override
    public void afterPropertiesSet()
    {
        eventPublisher.register(this);
    }

    @Override
    public void destroy()
    {
        //JRADEV-12770: We need this call because the plugins system does not send "pluginDisabled" events on plugin
        //system shutdown. Arrrh.
        shutdown();
    }

    @EventListener
    public void pluginEnabled(PluginEnabledEvent event)
    {
        if (PLUGIN_KEY.equals(event.getPlugin().getKey()))
        {
            state.onPluginEnabled();
        }
    }

    @EventListener
    public void pluginDisabled(PluginDisabledEvent event)
    {
        if (PLUGIN_KEY.equals(event.getPlugin().getKey()))
        {
            shutdown();
        }
    }

    @EventListener
    public void featureEnabled(FeatureEnabledEvent event)
    {
        // this feature toggle only works for OnDemand
        if (monitoringFeature.isControlledBy(event))
        {
            state.onFeatureEnabled();
        }
    }

    @EventListener
    public void featureDisabled(FeatureDisabledEvent event)
    {
        // this feature toggle only works for OnDemand
        if (monitoringFeature.isControlledBy(event))
        {
            state.onFeatureDisabled();
        }
    }

    private void shutdown()
    {
        eventPublisher.unregister(this);
        state.onPluginDisabled();
    }

    private void startMonitoring()
    {
        for (MonitorService monitorService : monitorServices)
        {
            log.debug("Starting {}", monitorService);
            try
            {
                monitorService.start();
            }
            catch (Exception e)
            {
                log.error("Failed to start: " + monitorService, e);
            }
        }
        log.info("Started JIRA monitoring");
    }

    private void stopMonitoring()
    {
        // stop in reverse order to start
        for (ListIterator<MonitorService> it = monitorServices.listIterator(monitorServices.size()); it.hasPrevious() ;)
        {
            MonitorService monitorService = it.previous();
            log.debug("Stopping {}", monitorService);
            try
            {
                monitorService.stop();
            }
            catch (Exception e)
            {
                log.error("Failed to stop " + monitorService, e);
            }
        }
        log.info("Stopped JIRA monitoring");
    }

    protected void changeStateTo(State nextState)
    {
        log.debug("Changing state from '{}' to '{}'", this.state.getClass().getSimpleName(), nextState.getClass().getSimpleName());
        this.state = nextState;
        nextState.init();
    }

    private static class State
    {
        // init this state
        void init()
        {}

        // event: monitoring plugin enabled
        void onPluginEnabled()
        {}

        // event: monitoring plugin disabled
        void onPluginDisabled()
        {}

        // event: monitoring feature enabled
        void onFeatureEnabled()
        {}

        // event: monitoring feature disabled
        void onFeatureDisabled()
        {}
    }

    private class Disabled extends State
    {
        @Override
        public void onPluginEnabled()
        {
            // default to NotMonitoring in OnDemand, but allow overriding via feature toggle
            if (!ON_DEMAND.isSystemPropertyEnabled() || monitoringFeature.enabled())
            {
                changeStateTo(new Monitoring());
            }
        }

        @Override
        void onFeatureEnabled()
        {
            changeStateTo(new Monitoring());
        }
    }

    /**
     * Plugin is actively monitoring.
     */
    private class Monitoring extends State
    {
        @Override
        public void init()
        {
            startMonitoring();
        }

        @Override
        void onPluginDisabled()
        {
            stopMonitoring();
            changeStateTo(new Disabled());
        }

        @Override
        void onFeatureDisabled()
        {
            onPluginDisabled();
        }
    }
}

package com.atlassian.upm;

import java.util.Date;
import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.sal.api.scheduling.PluginScheduler;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpmSchedulerTest
{
    @Mock PluginScheduler pluginScheduler;
    @Mock EventPublisher eventPublisher;
    @Mock PluginJob pluginJob;
    @Mock PluginEnabledEvent pluginEnabledEvent;
    @Mock Plugin plugin;

    private TestScheduler scheduler;

    @Before
    public void setup()
    {
        when(pluginEnabledEvent.getPlugin()).thenReturn(plugin);
        
        scheduler = new TestScheduler(pluginScheduler, eventPublisher);
        simulateSystemInitialization();
    }

    @Test
    public void assertThatSchedulerIsRegisteredToListenToEventsUponSystemInitialization()
    {
        verify(eventPublisher, times(1)).register(scheduler);
    }

    @Test
    public void assertThatSchedulerUnregistersItselfFromListeningToEventsUponUpmShutdown()
    {
        simulateUpmShutDown();
        verify(eventPublisher, times(1)).unregister(scheduler);
    }

    @Test
    public void assertThatSchedulerIsScheduledUponSystemInitialization()
    {
        verify(pluginScheduler, times(1)).scheduleJob(anyString(), any(Class.class), any(Map.class), any(Date.class), anyLong());
    }

    @Test
    public void assertThatSchedulerIsUnscheduledOnceUponSystemInitialization()
    {
        //must always unschedule before scheduling in case it was previously scheduled.
        verify(pluginScheduler, times(1)).unscheduleJob(anyString());
    }

    @Test
    public void assertThatSchedulerIsUnscheduledAgainUponUpmShutdown()
    {
        simulateUpmShutDown();
        verify(pluginScheduler, times(2)).unscheduleJob(anyString());
    }

    @Test
    public void assertThatSchedulerIsExecutedUponUpmEnablement()
    {
        when(plugin.getKey()).thenReturn("com.atlassian.upm.atlassian-universal-plugin-manager-plugin");
        scheduler.onPluginEnabled(pluginEnabledEvent);
        verify(pluginJob, times(1)).execute(any(Map.class));
    }

    @Test
    public void assertThatSchedulerIsNotExecutedUponRandomPluginEnablement()
    {
        when(plugin.getKey()).thenReturn("some-random-key");
        scheduler.onPluginEnabled(pluginEnabledEvent);
        verify(pluginJob, never()).execute(any(Map.class));
    }

    private void simulateSystemInitialization()
    {
        scheduler.onStart();
    }

    private void simulateUpmShutDown()
    {
        try
        {
            scheduler.destroy();
        }
        catch (Exception e)
        {
            fail("Exception was thrown: " + e);
        }
    }

    private class TestScheduler extends UpmScheduler
    {
        TestScheduler(PluginScheduler pluginScheduler, EventPublisher eventPublisher)
        {
            super(pluginScheduler, eventPublisher);
        }

        @Override
        public PluginJob getPluginJob()
        {
            return pluginJob;
        }

        @Override
        public Map<String, Object> getJobData()
        {
            return ImmutableMap.of();
        }
    }
}

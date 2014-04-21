package com.atlassian.sal.jira.scheduling;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.service.JiraService;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.JiraServiceContainerImpl;
import com.atlassian.jira.service.ServiceException;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.opensymphony.module.propertyset.PropertySet;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.longThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import org.mockito.internal.matchers.LessThan;

public class TestJiraPluginScheduler
{
    private ServiceManager serviceManager;
    private JiraPluginScheduler scheduler;

    @Before
    public void setUp() throws Exception
    {
        serviceManager = Mockito.mock(ServiceManager.class);
        scheduler = new JiraPluginScheduler(serviceManager);
    }

    @Test
    public void testScheduleAndRepeat()
            throws SearchException, GenericEntityException, ServiceException, ObjectConfigurationException
    {
        PropertySet mockPropertySet = Mockito.mock(PropertySet.class);
        when(mockPropertySet.getString(JiraPluginScheduler.REPEAT_INTERVAL)).thenReturn(null);

        JiraService mockService = Mockito.mock(JiraService.class);
        when(mockService.getName()).thenReturn("myService");
        when(mockService.getProperties()).thenReturn(mockPropertySet);
        JiraServiceContainer serviceContainer = new JiraServiceContainerImpl(mockService, 111L);

        when(serviceManager.addService(eq("myjob"), eq(JiraPluginSchedulerService.class), longThat(new LessThan(1001L)), anyMap())).thenReturn(serviceContainer);

        scheduler.scheduleJob("myjob", PluginJob.class, new HashMap<String, Object>(), (new DateTime()).plusMillis(1000).toDate(), 50);
    }

    @Test
    public void testScheduleNow() throws SearchException, GenericEntityException, ServiceException
    {
        scheduler.scheduleJob("myjob", PluginJob.class, new HashMap<String, Object>(), (new DateTime()).toDate(), 1500);
        verify(serviceManager, times(1)).addService(eq("myjob"), eq(JiraPluginSchedulerService.class), eq(20L), anyMap());
    }

    @Test
    public void testScheduleLegacyJobWithNoRepeatInterval() throws Exception
    {
        PropertySet mockPropertySet = Mockito.mock(PropertySet.class);
        when(mockPropertySet.getString(JiraPluginScheduler.REPEAT_INTERVAL)).thenReturn(null);

        JiraService mockService = Mockito.mock(JiraService.class);
        when(mockService.getName()).thenReturn("myService");
        when(mockService.getProperties()).thenReturn(mockPropertySet);

        JiraServiceContainer serviceContainer = new JiraServiceContainerImpl(mockService, 111L);
        serviceContainer.setDelay(123);

        when(serviceManager.getServices()).thenReturn(Collections.singleton(serviceContainer));

        scheduler.reconfigureAfterFirstFire("myService");
        verify(serviceManager).editServiceByName(eq("myService"), eq(123L), Matchers.anyMap());
    }

    @Test
    public void testScheduleWithValidRepeatInterval() throws Exception
    {
        PropertySet mockPropertySet = mock(PropertySet.class);
        when(mockPropertySet.getString(JiraPluginScheduler.REPEAT_INTERVAL)).thenReturn("456");

        JiraService mockService = mock(JiraService.class);
        when(mockService.getName()).thenReturn("myService");
        when(mockService.getProperties()).thenReturn(mockPropertySet);

        JiraServiceContainer serviceContainer = new JiraServiceContainerImpl(mockService, 111L);
        serviceContainer.setDelay(123);

        when(serviceManager.getServices()).thenReturn(Collections.singleton(serviceContainer));

        scheduler.reconfigureAfterFirstFire("myService");
        verify(serviceManager).editServiceByName(eq("myService"), eq(456L), Matchers.anyMap());
    }

    @Test
    public void testScheduleWithInvalidRepeatInterval() throws Exception
    {
        PropertySet mockPropertySet = mock(PropertySet.class);
        when(mockPropertySet.getString(JiraPluginScheduler.REPEAT_INTERVAL)).thenReturn("notANumber");

        JiraService mockService = mock(JiraService.class);
        when(mockService.getName()).thenReturn("myService");
        when(mockService.getProperties()).thenReturn(mockPropertySet);

        JiraServiceContainer serviceContainer = new JiraServiceContainerImpl(mockService, 111L);
        serviceContainer.setDelay(123);

        when(serviceManager.getServices()).thenReturn(Collections.singleton(serviceContainer));

        scheduler.reconfigureAfterFirstFire("myService");
        verify(serviceManager).editServiceByName(eq("myService"), eq(123L), Matchers.anyMap());
    }

}

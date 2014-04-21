package com.atlassian.jira.service;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.plugin.MockComponentClassManager;
import com.atlassian.jira.util.EasyList;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Test for JRA-15879
 */
public class TestDefaultServiceManagerConcurrentModEx extends LegacyJiraMockTestCase
{
    public void testGetServicesDoesntThrowConcurrentMod() throws Exception
    {
        final List<JiraServiceContainer> services = EasyList.<JiraServiceContainer>build(new MockJiraServiceContainer.Builder().id(1).build(),
                new MockJiraServiceContainer.Builder().id(2).build());
        final DefaultServiceManager manager = new DefaultServiceManager(new MockServiceConfigStore()
        {
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return services;
            }

            @Override
            public JiraServiceContainer addServiceConfig(final String name, final Class<? extends JiraService> clazz, final long delay)
            {
                return new MockJiraServiceContainer.Builder().id(3).build();
            }
        }, new MockComponentClassManager(), null, null, null);

        assertListsEquals(services, manager.getServices());

        final Iterator<JiraServiceContainer> it = manager.getServices().iterator();
        it.next();
        manager.addService("fred", MockJiraServiceContainer.class.getName(), 1000000);
        //shouldn't throw ConcurrentModificationException
        it.next();
    }

    public void testGetServicesGetsUsableOnesWhenNoneAreDue() throws Exception
    {
        final List<JiraServiceContainer> expectedList = new ArrayList<JiraServiceContainer>();

        final List<JiraServiceContainer> inputList = ImmutableList.<JiraServiceContainer>of(
                new MockJiraServiceContainer.Builder().id(1).build(),
                new MockJiraServiceContainer.Builder().id(2).build(),
                new MockJiraServiceContainer.Builder().id(3).build()
        );

        final DefaultServiceManager manager = new DefaultServiceManager(new MockServiceConfigStore()
        {
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return inputList;
            }
        }, new MockComponentClassManager(), null, null, null);

        assertListsEquals(expectedList, makeList(manager.getServicesForExecution(1234)));
    }

    public void testGetServicesGetsUsableOnesThatAreDue() throws Exception
    {
        final MockJiraServiceContainer expectedService =
                new MockJiraServiceContainer.Builder().id(2).usable(true).running(false).due(true).build();

        final List<JiraServiceContainer> expectedList = ImmutableList.<JiraServiceContainer>of(expectedService);

        final List<JiraServiceContainer> inputList = ImmutableList.<JiraServiceContainer>of(
                new MockJiraServiceContainer.Builder().id(1).build(),
                expectedService,
                new MockJiraServiceContainer.Builder().id(3).build()
        );

        final DefaultServiceManager manager = new DefaultServiceManager(new MockServiceConfigStore()
        {
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return inputList;
            }
        }, new MockComponentClassManager(), null, null, null);

        assertListsEquals(expectedList, makeList(manager.getServicesForExecution(1234)));
    }

    public void testGetServicesGetsUsableOnesThatAreNotRunning() throws Exception
    {
        final MockJiraServiceContainer expectedService = new MockJiraServiceContainer.Builder().id(2).usable(true).due(true).build();
        final List<JiraServiceContainer> expectedList = ImmutableList.<JiraServiceContainer>of(expectedService);


        final MockJiraServiceContainer thisOneIsRunning = new MockJiraServiceContainer.Builder().id(4).usable(true).running(true).due(true).build();
        final List<JiraServiceContainer> inputList = ImmutableList.<JiraServiceContainer>of(
                new MockJiraServiceContainer.Builder().id(1).build(),
                expectedService,
                thisOneIsRunning,
                new MockJiraServiceContainer.Builder().id(3).build()
        );

        final DefaultServiceManager manager = new DefaultServiceManager(new MockServiceConfigStore()
        {
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return inputList;
            }
        }, new MockComponentClassManager(), null, null, null);

        assertListsEquals(expectedList, makeList(manager.getServicesForExecution(1234)));
    }

    void assertListsEquals(final List<JiraServiceContainer> expected, final Collection<JiraServiceContainer> got)
    {
        final Comparator<JiraServiceContainer> dumbIdComparator = new Comparator<JiraServiceContainer>()
        {
            public int compare(final JiraServiceContainer o1, final JiraServiceContainer o2)
            {
                return (int) (o1.getId() - o2.getId());
            }
        };

        final List<JiraServiceContainer> sortedExpectedServices = new ArrayList<JiraServiceContainer>(expected);
        Collections.sort(sortedExpectedServices, dumbIdComparator);

        final List<JiraServiceContainer> sortedGotServices = new ArrayList<JiraServiceContainer>(got);
        Collections.sort(sortedGotServices, dumbIdComparator);

        assertEquals(expected, sortedGotServices);
    }

    private Collection<JiraServiceContainer> makeList(final Iterable<JiraServiceContainer> servicesForExecution)
    {
        List<JiraServiceContainer> list = new ArrayList<JiraServiceContainer>();
        for (JiraServiceContainer jiraServiceContainer : servicesForExecution)
        {
            list.add(jiraServiceContainer);
        }
        return list;
    }
}

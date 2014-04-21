package com.atlassian.jira.service;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.plugin.MockComponentClassManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.services.DebugService;
import com.atlassian.jira.service.services.export.ExportService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDefaultServiceManager extends LegacyJiraMockTestCase
{
    public TestDefaultServiceManager(String s)
    {
        super(s);
    }

    public void testAddService() throws Exception
    {
        ServiceConfigStore serviceConfigStore = ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class);
        DefaultServiceManager dsm = new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), null, null);
        final String name = "test service";

        JiraServiceContainer jiraServiceContainer = dsm.addService(name, "com.atlassian.jira.service.services.DebugService", 500);

        // make sure this is the only service configured
        assertTrue(dsm.getServices().size() == 1);

        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

    public void testAddServiceWithParams() throws Exception
    {
        ServiceConfigStore serviceConfigStore = ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class);
        DefaultServiceManager dsm = new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), null, null);
        final String name = "test service";

        Map<String, String[]> params = MapBuilder.newBuilder("DIR_NAME", new String[]{"/tmp/"}).toMap();

        JiraServiceContainer jiraServiceContainer = dsm.addService(name, ExportService.class.getName(), 500, params);

        // make sure this is the only service configured
        assertTrue(dsm.getServices().size() == 1);

        assertNotNull(jiraServiceContainer.getProperties());
        assertEquals("/tmp/", jiraServiceContainer.getProperty("DIR_NAME"));

        jiraServiceContainer = dsm.getServiceWithId(jiraServiceContainer.getId());
        assertNotNull(jiraServiceContainer.getProperties());
        assertEquals("/tmp/", jiraServiceContainer.getProperty("DIR_NAME"));

        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

    public void testAddServiceFailsForBadService() throws Exception
    {
        ServiceConfigStore serviceConfigStore = ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class);
        DefaultServiceManager dsm = new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), null, null);
        final String name = "test service";

        try
        {
            dsm.addService(name, "com.stuff.otherStuff.ServiceClass", 500);
            fail("ClassNotFoundException expected.");
        }
        catch (ClassNotFoundException yay)
        {
            // Expected.
        }
    }

    public void testEditServiceByName() throws Exception
    {
        ServiceConfigStore serviceConfigStore = ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class);
        DefaultServiceManager dsm = new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), null, null);
        final String name = "test service";

        dsm.addService(name, "com.atlassian.jira.service.services.DebugService", 500);

        dsm.editServiceByName(name, 8, new HashMap<String, String[]>());

        JiraServiceContainer jiraServiceContainer = dsm.getServiceWithName(name);

        assertEquals(jiraServiceContainer.getDelay(), 8);

        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

    public void testEditServiceById() throws Exception
    {
        ServiceConfigStore serviceConfigStore = ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class);
        DefaultServiceManager dsm = new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), null, null);
        final String name = "test service";

        JiraServiceContainer jiraServiceContainer = dsm.addService(name, "com.atlassian.jira.service.services.DebugService", 500);

        dsm.editService(jiraServiceContainer.getId(), 8, new HashMap<String, String[]>());

        jiraServiceContainer = dsm.getServiceWithId(jiraServiceContainer.getId());

        assertEquals(jiraServiceContainer.getDelay(), 8);

        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

    public void testContainsServiceWithId() throws Exception
    {
        ServiceConfigStore serviceConfigStore = ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class);
        DefaultServiceManager dsm = new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), null, null);
        final String name = "test service";

        JiraServiceContainer jiraServiceContainer = dsm.addService(name, "com.atlassian.jira.service.services.DebugService", 500);

        assertTrue(dsm.containsServiceWithId(jiraServiceContainer.getId()));
        dsm.removeService(jiraServiceContainer.getId());

        assertTrue(dsm.getServices().isEmpty());
    }

    public void testRemoveServiceByNameNoService() throws GenericEntityException
    {
        DefaultServiceManager dsm = new DefaultServiceManager(ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class), null, null, null);
        final String name = "test service";

        try
        {
            dsm.removeServiceByName(name);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("No services with name '" + name + "' exist.", e.getMessage());
        }
        catch (Exception ex)
        {
            fail();
        }
    }

    public void testRemoveServiceByNameMultipleServices() throws GenericEntityException
    {
        final String name = "test service";
        UtilsForTests.getTestEntity("ServiceConfig", EasyMap.build("name", name));
        UtilsForTests.getTestEntity("ServiceConfig", EasyMap.build("name", name));

        DefaultServiceManager dsm = new DefaultServiceManager(ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class), null, null, null);

        try
        {
            dsm.removeServiceByName(name);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Multiple services with name '" + name + "' exist.", e.getMessage());
        }
        catch (Exception ex)
        {
            fail();
        }
    }

    public void testRemoveServiceByName() throws Exception
    {
        final String name = "test service";
        UtilsForTests.getTestEntity("ServiceConfig", EasyMap.build("name", name, "time", new Long(1), "clazz", DebugService.class.getName()));

        DefaultServiceManager dsm = new DefaultServiceManager(ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class), null, null, null);

        dsm.removeServiceByName(name);

        final List services = CoreFactory.getGenericDelegator().findByAnd("ServiceConfig", EasyMap.build("name", name));
        assertTrue(services.isEmpty());
    }

    public void testRemoveService() throws Exception
    {
        final GenericValue service = UtilsForTests.getTestEntity("ServiceConfig", EasyMap.build("name", "test service", "time", new Long(1), "clazz", DebugService.class.getName()));

        DefaultServiceManager dsm = new DefaultServiceManager(ComponentManager.getComponentInstanceOfType(ServiceConfigStore.class), null, null, null);

        dsm.removeService(service.getLong("id"));

        final List services = CoreFactory.getGenericDelegator().findByAnd("ServiceConfig", EasyMap.build("name", "test service"));
        assertTrue(services.isEmpty());
    }

    public void testAnonymousUsersShouldNotBeAbleToManageAnyService() throws Exception
    {
        final List<JiraServiceContainer> storedServiceConfigs =
                ImmutableList.<JiraServiceContainer>of(
                        new MockJiraServiceContainer.Builder().id(1).build(),
                        new MockJiraServiceContainer.Builder().id(2).build(),
                        new MockJiraServiceContainer.Builder().id(3).build(),
                        new MockJiraServiceContainer.Builder().id(4).build()
                );

        ServiceConfigStore serviceConfigStore = new MockServiceConfigStore(){
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return storedServiceConfigs;
            }
        };

        DefaultServiceManager serviceManager =
                new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), null, null);

        User anAnonymousUser = null;
        Iterable<JiraServiceContainer> actualServicesManageableByAnAnonymousUser = serviceManager.getServicesManageableBy(anAnonymousUser);

        assertTrue(Iterables.isEmpty(actualServicesManageableByAnAnonymousUser));
    }

    public void testSystemAdminUsersShouldBeAbleToManageAllServices() throws Exception
    {
        final List<JiraServiceContainer> storedServiceConfigs =
                ImmutableList.<JiraServiceContainer>of(
                        new MockJiraServiceContainer.Builder().id(1).build(),
                        new MockJiraServiceContainer.Builder().id(2).build(),
                        new MockJiraServiceContainer.Builder().id(3).build(),
                        new MockJiraServiceContainer.Builder().id(4).build()
                );

        ServiceConfigStore serviceConfigStore = new MockServiceConfigStore(){
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return storedServiceConfigs;
            }
        };


        User aSysAdmin = new MockUser("system-admin");

        MockPermissionManager mockPermissionManager = new MockPermissionManager(){
            @Override
            public boolean hasPermission(int permissionsId, User u)
            {
                if (Permissions.SYSTEM_ADMIN == permissionsId)
                {
                   return true;
                }
                else if (Permissions.ADMINISTER == permissionsId)
                {
                    return false;
                }
                throw new UnsupportedOperationException();
            }
        };

        DefaultServiceManager serviceManager =
                new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), mockPermissionManager, null);

        Iterable<JiraServiceContainer> actualServicesManageableByASysAdmin = serviceManager.getServicesManageableBy(aSysAdmin);

        assertTrue(Iterables.elementsEqual(storedServiceConfigs, actualServicesManageableByASysAdmin));
    }

    public void testAdminUsersShouldBeAbleToManageOnlyPopAndImapServices() throws Exception
    {
        final List<JiraServiceContainer> expectedServiceConfigsForAnAdminUser =
                ImmutableList.<JiraServiceContainer>of(
                        new MockJiraServiceContainer.Builder().id(1).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build(),
                        new MockJiraServiceContainer.Builder().id(2).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build(),
                        new MockJiraServiceContainer.Builder().id(5).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build()
                );

        final List<JiraServiceContainer> storedServiceConfigs =
                ImmutableList.<JiraServiceContainer>of(
                        new MockJiraServiceContainer.Builder().id(1).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build(),
                        new MockJiraServiceContainer.Builder().id(2).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build(),
                        new MockJiraServiceContainer.Builder().id(3).serviceClass("com.test.TestService").build(),
                        new MockJiraServiceContainer.Builder().id(4).serviceClass("com.test.TestService").build(),
                        new MockJiraServiceContainer.Builder().id(5).serviceClass("com.atlassian.jira.service.services.mail.MailFetcherService").build()
                );

        ServiceConfigStore serviceConfigStore = new MockServiceConfigStore(){
            @Override
            public Collection<JiraServiceContainer> getAllServiceConfigs()
            {
                return storedServiceConfigs;
            }
        };

        User anAdminUser = new MockUser("jira-admin");

        MockPermissionManager mockPermissionManager = new MockPermissionManager(){
            @Override
            public boolean hasPermission(int permissionsId, User u)
            {
                if (Permissions.SYSTEM_ADMIN == permissionsId)
                {
                   return false;
                }
                else if (Permissions.ADMINISTER == permissionsId)
                {
                    return true;
                }
                throw new UnsupportedOperationException();
            }
        };

        DefaultServiceManager serviceManager =
                new DefaultServiceManager(serviceConfigStore, new MockComponentClassManager(), mockPermissionManager, new DefaultInBuiltServiceTypes(mockPermissionManager));

        Iterable<JiraServiceContainer> actualServicesManageableByAnAdminUser = serviceManager.getServicesManageableBy(anAdminUser);

        assertListsEquals(Lists.newArrayList(expectedServiceConfigsForAnAdminUser), Lists.newArrayList(actualServicesManageableByAnAdminUser));
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

    public void testServiceScheduleSkipper()
    {
        DefaultServiceManager.ServiceScheduleSkipperImpl skipper = new DefaultServiceManager.ServiceScheduleSkipperImpl();

        final Long SERVICE_1 = 1L;
        assertFalse(skipper.complete(SERVICE_1));
        assertTrue(skipper.addService(SERVICE_1));
        assertFalse(skipper.addService(SERVICE_1));
        assertFalse(skipper.addService(SERVICE_1));

        final Long SERVICE_2 = 2L;
        assertFalse(skipper.complete(SERVICE_2));
        assertTrue(skipper.addService(SERVICE_2));
        assertFalse(skipper.addService(SERVICE_2));
        assertFalse(skipper.addService(SERVICE_2));
    }

    public void testServiceScheduleSkipperCheckAndRemove()
    {
        DefaultServiceManager.ServiceScheduleSkipperImpl skipper = new DefaultServiceManager.ServiceScheduleSkipperImpl();

        final Long SERVICE_1 = 1L;
        assertTrue(skipper.addService(SERVICE_1));
        final Long SERVICE_2 = 2L;
        assertTrue(skipper.addService(SERVICE_2));
        final Long SERVICE_3 = 3L;
        assertTrue(skipper.addService(SERVICE_3));

        // returns true first time around
        assertTrue(skipper.complete(SERVICE_2));
        assertFalse(skipper.complete(SERVICE_2));
        assertFalse(skipper.complete(SERVICE_2));

        // returns true first time around
        assertTrue(skipper.complete(SERVICE_1));
        assertFalse(skipper.complete(SERVICE_1));
        assertFalse(skipper.complete(SERVICE_1));

        // returns false as 0 is not in the skipper
        assertFalse(skipper.complete(0L));
    }
}

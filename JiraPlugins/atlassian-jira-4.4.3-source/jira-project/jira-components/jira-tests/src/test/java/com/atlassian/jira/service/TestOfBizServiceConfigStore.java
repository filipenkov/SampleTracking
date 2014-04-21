package com.atlassian.jira.service;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.service.services.DebugService;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
public class TestOfBizServiceConfigStore extends LegacyJiraMockTestCase
{
    public void testRemoveServiceConfigCleansProperties() throws Exception
    {
        final AtomicBoolean removeCalled = new AtomicBoolean(false);

        // mock serviceConfigGV
        final GenericValue serviceConfigGV = new MockGenericValue("ServiceConfig")
        {
            @Override
            public void remove() throws GenericEntityException
            {
                removeCalled.set(true);
            }
        };

        final AtomicBoolean removePropertySetCalled = new AtomicBoolean(false);
        final OfBizServiceConfigStore store = new OfBizServiceConfigStore(null, null)
        {
            @Override
            GenericValue getGenericValueForConfig(final JiraServiceContainer config) throws GenericEntityException
            {
                return serviceConfigGV;
            }

            @Override
            void removePropertySet(final GenericValue gv)
            {
                removePropertySetCalled.set(true);
                assertSame(serviceConfigGV, gv);
            }
        };

        final JiraServiceContainer config = new JiraServiceContainerImpl(null, null);
        store.removeServiceConfig(config);

        // assertions
        assertTrue(removeCalled.get());
        assertTrue(removePropertySetCalled.get());
    }

    //JRA-20419
    public void testRuntimeExceptionDuringInitialisation() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final ComponentClassManager clsMgr = control.createMock(ComponentClassManager.class);

        EasyMock.expect(clsMgr.<JiraService>newInstance("badClass")).andThrow(new RuntimeException("Some random exception"));
        EasyMock.expect(clsMgr.<JiraService>newInstance("goodClass")).andReturn(new DebugService());
        
        control.replay();

        OfBizServiceConfigStore cs = createStore(clsMgr);

        createServiceConfig(10001, 10, "badClass", "Broken");
        createServiceConfig(10002, 11, "goodClass", "Good");

        final Collection<JiraServiceContainer> configs = new ArrayList<JiraServiceContainer>(cs.getAllServiceConfigs());
        assertEquals(2, configs.size());

        JiraServiceContainer currentService = findServiceById(configs, 10001);
        assertNotNull(currentService);
        assertTrue(currentService instanceof UnloadableJiraServiceContainer);
        assertEquals("Broken", currentService.getName());
        assertEquals(10, currentService.getDelay());

        currentService = findServiceById(configs, 10002);
        assertNotNull(currentService);
        assertEquals("Good", currentService.getName());
        assertEquals(11, currentService.getDelay());
        assertTrue(currentService instanceof JiraServiceContainerImpl);

        control.verify();
    }

    private JiraServiceContainer findServiceById(Collection<JiraServiceContainer> services, long id)
    {
        for (Iterator<JiraServiceContainer> iterator = services.iterator(); iterator.hasNext();)
        {
            JiraServiceContainer jiraService = iterator.next();
            if (jiraService.getId() == id)
            {
                iterator.remove();
                return jiraService;
            }
        }
        return null;
    }

    private OfBizServiceConfigStore createStore(ComponentClassManager classMgr)
    {
        final GenericDelegator delegator = ComponentManager.getComponentInstanceOfType(GenericDelegator.class);
        return new OfBizServiceConfigStore(delegator, classMgr);
    }

    private GenericValue createServiceConfig(long id, long delayTime, String klazz, String name)
    {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("time", delayTime);
        map.put("clazz", klazz);
        map.put("name", name);

        return UtilsForTests.getTestEntity("ServiceConfig", map);
    }
}

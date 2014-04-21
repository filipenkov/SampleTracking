package com.atlassian.jira.mock;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.MockComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentAccessorWorker;
import com.atlassian.jira.mock.multitenant.MockMultiTenantComponentFactory;
import com.atlassian.jira.mock.multitenant.MockTenant;
import com.atlassian.jira.mock.multitenant.MockTenantReference;
import com.atlassian.jira.mock.pico.MockPicoContainer;
import com.atlassian.multitenant.MultiTenantContext;
import org.powermock.api.easymock.PowerMock;

import static org.easymock.EasyMock.expect;

/**
 * Use this class to hack the heck out of the component manager so that it does not start the whole
 * freaking world to satisfy a single unit test.
 *
 * @deprecated use {@link com.atlassian.jira.component.ComponentAccessor} instead of {@link com.atlassian.jira.ComponentManager}
 * in your code, and the {@link com.atlassian.jira.junit.rules.MockComponentContainer} rule in the tests.
 */
@Deprecated
public class ComponentManagerMocker
{
    public void doMock()
    {
        // Ensure that the multitenantfactory is not null
        MultiTenantContext.setFactory(new MockMultiTenantComponentFactory(new MockTenantReference(new MockTenant("tenant"))));
        // Initialise the ComponentAccessor
        ComponentAccessor.initialiseWorker(new ComponentAccessorWorker());

        // Set up Mocks
        PowerMock.mockStaticPartial(ComponentManager.class, "getInstance");
        expect(ComponentManager.getInstance()).andReturn(new MockComponentManager()).anyTimes();
        PowerMock.replay(ComponentManager.class);
    }


    public <T> ComponentManagerMocker addComponent(Class<T> componentClass, T compInstance)
    {
        getMockContainer().addComponent(componentClass, compInstance);
        return this;
    }

    private MockPicoContainer getMockContainer()
    {
        return (MockPicoContainer) ComponentManager.getInstance().getContainer();
    }
}

package com.atlassian.jira;

import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;

import java.util.Collection;

import static com.atlassian.multitenant.MultiTenantComponentMap.Registration.NO;

/**
 * Holder for the multi-tenant component map.
 *
 * @since v5.1
 */
class ComponentManagerMap
{
    static final MultiTenantComponentMap<ComponentManager> COMPONENT_MANAGER_MAP;
    static
    {
        // go go multi tenancy!
        if (MultiTenantContext.isEnabled())
        {
            // We do not want the MultiTenantManager to handle the destruction of the ComponentManager
            // so we do not register the listener. Instead it is handled explicitly by the JiraLauncher.
            COMPONENT_MANAGER_MAP = MultiTenantContext.getFactory().createComponentMapBuilder(new ComponentManagerCreator()).registerListener(NO).construct();
        }
        else
        {
            // this map just throws exceptions when used
            COMPONENT_MANAGER_MAP = new ContextNotInitialised();
        }
    }

    private static final String NOT_INITIALISED = "MultiTenantContext not initialised! If this is a unit test, you"
            + " need to call MultiTenantContextTestUtils.setupMultiTenantSystem() in the set up code (this class is"
            + " in the jira-tests Maven module)";

    /**
     * Creates ComponentManager instances for tenants.
     */
    private static class ComponentManagerCreator implements MultiTenantCreator<ComponentManager>
    {
        @Override
        public ComponentManager create(Tenant tenant)
        {
            return new ComponentManager();
        }
    }

    /**
     * This class just throws IllegalStateException from all its methods.
     */
    private static class ContextNotInitialised implements MultiTenantComponentMap<ComponentManager>
    {
        @Override
        public ComponentManager get() throws IllegalStateException
        {
            throw new IllegalStateException(NOT_INITIALISED);
        }

        @Override
        public Collection<ComponentManager> getAll()
        {
            throw new IllegalStateException(NOT_INITIALISED);
        }

        @Override
        public void initialiseAll()
        {
            throw new IllegalStateException(NOT_INITIALISED);
        }

        @Override
        public boolean isInitialised() throws IllegalStateException
        {
            throw new IllegalStateException(NOT_INITIALISED);
        }

        @Override
        public void addInstance(ComponentManager object) throws IllegalStateException
        {
            throw new IllegalStateException(NOT_INITIALISED);
        }

        @Override
        public void destroy()
        {
            throw new IllegalStateException(NOT_INITIALISED);
        }

        @Override
        public void onTenantStart(Tenant tenant)
        {
            throw new IllegalStateException(NOT_INITIALISED);
        }

        @Override
        public void onTenantStop(Tenant tenant)
        {
            throw new IllegalStateException(NOT_INITIALISED);
        }
    }
}

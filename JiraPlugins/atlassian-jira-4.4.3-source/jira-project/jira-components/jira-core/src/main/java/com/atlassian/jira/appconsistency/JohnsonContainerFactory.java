package com.atlassian.jira.appconsistency;

import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.setup.ContainerFactory;
import com.atlassian.multitenant.MultiTenantComponentFactory;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;

import java.util.Map;

/**
 * This, plus a corresponding entry in johnson-config.xml, makes Johnson MultiTenant-aware.
 * @since v4.3
 */
public class JohnsonContainerFactory implements ContainerFactory
{
    @Override
    public JohnsonEventContainer create()
    {
        final MultiTenantComponentFactory factory = MultiTenantContext.getFactory();
        return factory.createEnhancedComponent(new MultiTenantCreator<JohnsonEventContainer>()
        {
            @Override
            public JohnsonEventContainer create(Tenant tenant)
            {
                return new JohnsonEventContainer();
            }
        }, JohnsonEventContainer.class);
    }

    @Override
    public void init(Map params)
    {
    }
}

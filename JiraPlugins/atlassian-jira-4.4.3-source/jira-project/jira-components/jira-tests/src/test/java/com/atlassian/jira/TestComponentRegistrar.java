package com.atlassian.jira;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import org.junit.Before;
import org.junit.Test;

public class TestComponentRegistrar extends ListeningTestCase
{
    @Before
    public void setUp()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    @Test
    public void testRegistrationStartupOK() throws Exception
    {
        new ContainerRegistrar().registerComponents(new ComponentContainer(), true);
    }

    @Test
    public void testRegistrationStartupBad() throws Exception
    {   
        new ContainerRegistrar().registerComponents(new ComponentContainer(), false);
    }
}

package com.atlassian.jira.projectconfig.tab;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.util.collect.MapBuilder;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertSame;

/**
 * @since v4.4
 */
public class TestDefaultProjectConfigTabManager
{
    @Test(expected = BeanInitializationException.class)
    public void testBadFactory()
    {
        DefaultProjectConfigTabManager manager = new DefaultProjectConfigTabManager();
        manager.setBeanFactory((BeanFactory) DuckTypeProxy.getProxy(BeanFactory.class, new Object()));
    }

    @Test
    public void testRegistrations()
    {
        IMocksControl control = EasyMock.createControl();

        MockTab mockTab1 = new MockTab("5");
        MockTab mockTab2 = new MockTab("6");


        ListableBeanFactory listableBeanFactory = control.createMock(ListableBeanFactory.class);
        expect(listableBeanFactory.getBeansOfType(ProjectConfigTab.class)).andReturn(MapBuilder.build("one", mockTab1, "two", mockTab2));

        control.replay();

        DefaultProjectConfigTabManager manager = new DefaultProjectConfigTabManager();
        manager.setBeanFactory(listableBeanFactory);

        assertSame(mockTab1, manager.getTabForId(mockTab1.getId()));
        assertSame(mockTab2, manager.getTabForId(mockTab2.getId()));

        control.verify();
    }

    private static class MockTab implements ProjectConfigTab
    {
        private final String id;

        public MockTab(String id)
        {
            this.id = id;
        }

        @Override
        public String getId()
        {
            return id;
        }

        @Override
        public String getLinkId()
        {
            return null;
        }

        @Override
        public String getTab(ProjectConfigTabRenderContext context)
        {
            return null;
        }

        @Override
        public String getTitle(ProjectConfigTabRenderContext context)
        {
            return null;
        }

        @Override
        public void addResourceForProject(ProjectConfigTabRenderContext context)
        {
        }
    }
}

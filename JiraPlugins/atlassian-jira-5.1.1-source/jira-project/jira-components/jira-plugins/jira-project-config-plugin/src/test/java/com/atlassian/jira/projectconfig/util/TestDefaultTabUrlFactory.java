package com.atlassian.jira.projectconfig.util;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.projectconfig.contextproviders.ContextProviderUtils;
import com.google.common.base.Function;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestDefaultTabUrlFactory
{
    @Test
    public void testForSummary()
    {
        assertForPath(null, new Function<DefaultTabUrlFactory, String>()
        {
            @Override
            public String apply(@Nullable DefaultTabUrlFactory from)
            {
                return from.forSummary();
            }
        });
    }

    @Test
    public void testForVersions()
    {
        assertForPath("versions", new Function<DefaultTabUrlFactory, String>()
        {
            @Override
            public String apply(@Nullable DefaultTabUrlFactory from)
            {
                return from.forVersions();
            }
        });
    }

    @Test
    public void testForComponents()
    {
        assertForPath("components", new Function<DefaultTabUrlFactory, String>()
        {
            @Override
            public String apply(@Nullable DefaultTabUrlFactory from)
            {
                return from.forComponents();
            }
        });
    }

    @Test
    public void testForIssueSecurity()
    {
        assertForPath("issuesecurity", new Function<DefaultTabUrlFactory, String>()
        {
            @Override
            public String apply(@Nullable DefaultTabUrlFactory from)
            {
                return from.forIssueSecurity();
            }
        });
    }

    @Test
    public void testForPermissions()
    {
        assertForPath("permissions", new Function<DefaultTabUrlFactory, String>()
        {
            @Override
            public String apply(@Nullable DefaultTabUrlFactory from)
            {
                return from.forPermissions();
            }
        });
    }

    @Test
    public void testForWorkflows()
    {
        assertForPath("workflows", new Function<DefaultTabUrlFactory, String>()
        {
            @Override
            public String apply(@Nullable DefaultTabUrlFactory from)
            {
                return from.forWorkflows();
            }
        });
    }

    @Test
    public void testForFields()
    {
        assertForPath("fields", new Function<DefaultTabUrlFactory, String>()
        {
            @Override
            public String apply(@Nullable DefaultTabUrlFactory from)
            {
                return from.forFields();
            }
        });
    }

    @Test
    public void testForScreens()
    {
        assertForPath("screens", new Function<DefaultTabUrlFactory, String>()
        {
            @Override
            public String apply(@Nullable DefaultTabUrlFactory from)
            {
                return from.forScreens();
            }
        });
    }

    private void assertForPath(String path, Function<DefaultTabUrlFactory, String> callback)
    {
        assertForPath(path, callback, "base/", "/base/");
        assertForPath(path, callback, "base", "/base/");
        assertForPath(path, callback, "/base", "/base/");
        assertForPath(path, callback, "/base/", "/base/");
        assertForPath(path, callback, "", "/");
    }

    private void assertForPath(String path, Function<DefaultTabUrlFactory, String> callback, final String base,
            final String urlStart)
    {
        MockProject project = new MockProject(2929L, "KEY");
        IMocksControl control = EasyMock.createControl();
        ContextProviderUtils utils = control.createMock(ContextProviderUtils.class);
        UrlEncoder encoder = control.createMock(UrlEncoder.class);

        EasyMock.expect(utils.getBaseUrl()).andReturn(base);
        EasyMock.expect(utils.getProject()).andReturn(project);
        EasyMock.expect(encoder.encode(project.getKey())).andReturn(project.getKey());

        if (path != null)
        {
            EasyMock.expect(encoder.encode(path)).andReturn(path);
        }

        control.replay();
        DefaultTabUrlFactory factory = new DefaultTabUrlFactory(utils, encoder);

        String expectedString = urlStart + "plugins/servlet/project-config/" + project.getKey();
        if (path != null)
        {
            expectedString = expectedString + "/" + path;
        }

        String actualInput = callback.apply(factory);
        assertEquals(expectedString, actualInput);

        control.verify();
    }
}

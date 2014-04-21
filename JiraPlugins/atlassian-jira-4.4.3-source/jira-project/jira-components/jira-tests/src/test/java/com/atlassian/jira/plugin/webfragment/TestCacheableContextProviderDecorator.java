package com.atlassian.jira.plugin.webfragment;

import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.MapBuilder;
import mock.servlet.MockHttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestCacheableContextProviderDecorator extends MockControllerTestCase
{
    @Mock
    private CacheableContextProvider contextProvider;

    private MockHttpServletRequest mockHttpServletRequest;

    @Before
    public void setUp()
    {
        mockHttpServletRequest = new MockHttpServletRequest();
    }

    @After
    public void tearDown()
    {
        mockHttpServletRequest = null;
    }

    @Test
    public void testGetContextMap()
    {
        final Map<String, Object> suppliedMap = MapBuilder.<String,Object>build(
                "lala", "dontcare",
                "mtan", "bleah"
        );

        final Map<String, Object> resultingMap = MapBuilder.<String,Object>build(
                "lolo", "wecare",
                "lele", "wedontcare"
        );

        expect(contextProvider.getContextMap(eq(suppliedMap))).andReturn(resultingMap);
        expect(contextProvider.getUniqueContextKey(eq(suppliedMap))).andStubReturn("aUniqueKey");

        mockController.replay();

        CacheableContextProviderDecorator decorator = getDecorator(contextProvider);

        Map<String, Object> contextMap = decorator.getContextMap(suppliedMap);

        assertEquals(resultingMap, contextMap);

        final String expectedKey = CacheableContextProviderDecorator.REQUEST_ATTRIBUTE_PREFIX +
                contextProvider.getClass().getName() + ":aUniqueKey";

        assertEquals(resultingMap, mockHttpServletRequest.getAttribute(expectedKey));

        // Doing it again since it's cached
        contextMap = decorator.getContextMap(suppliedMap);

        assertEquals(resultingMap, contextMap);

        mockController.verify();
    }


    private CacheableContextProviderDecorator getDecorator(final CacheableContextProvider contextProvider)
    {
        return new CacheableContextProviderDecorator(contextProvider)
        {
            @Override
            protected HttpServletRequest getRequest(Map<String, Object> context)
            {
                return mockHttpServletRequest;
            }
        };
    }
}

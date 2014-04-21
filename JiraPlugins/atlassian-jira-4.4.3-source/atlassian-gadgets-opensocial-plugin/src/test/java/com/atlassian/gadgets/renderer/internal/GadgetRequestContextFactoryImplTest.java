package com.atlassian.gadgets.renderer.internal;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.renderer.internal.GadgetRequestContextFactoryImpl.DEBUG_PROPERTY_KEY;
import static com.atlassian.gadgets.renderer.internal.GadgetRequestContextFactoryImpl.IGNORE_CACHE_PROPERTY_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetRequestContextFactoryImplTest
{
    @Mock LocaleResolver localeResolver;
    @Mock UserManager userManager;
    
    GadgetRequestContextFactory contextFactory;
    
    @Mock HttpServletRequest request;
    
    @Before
    public void setUp()
    {
        contextFactory = new GadgetRequestContextFactoryImpl(localeResolver, userManager);
    }
    
    @Test
    public void assertThatContextFactorySetsLocaleFromLocaleResolver()
    {
        when(localeResolver.getLocale(request)).thenReturn(Locale.US);
        assertThat(contextFactory.get(request).getLocale(), is(equalTo(Locale.US)));
    }
    
    @Test
    public void assertThatContextFactorySetsViewerToTheUserMakingTheRequest()
    {
        when(userManager.getRemoteUsername(request)).thenReturn("fred");
        assertThat(contextFactory.get(request).getViewer(), is(equalTo("fred")));        
    }
    
    @Test
    public void assertThatContextFactorySetsIgnoreCacheToFalseIfNoSystemPropertyOrRequestValueIsDefined()
    {
        assertFalse(contextFactory.get(request).getIgnoreCache());        
    }
    
    @Test
    public void assertThatContextFactorySetsIgnoreCacheToRequestValue()
    {
        when(request.getParameter("ignoreCache")).thenReturn("true");
        assertTrue(contextFactory.get(request).getIgnoreCache());        
    }
    
    @Test
    public void assertThatContextFactorySetsIgnoreCacheToSystemPropertyValue()
    {
        withSystemProperty(IGNORE_CACHE_PROPERTY_KEY, "true", new Runnable()
        {
            public void run()
            {
                assertTrue(contextFactory.get(request).getIgnoreCache());
            }
        });
    }
    
    @Test
    public void assertThatContextFactorySetsDebuggingEnabledToFalseIfNoSystemPropertyOrRequestValueIsDefined()
    {
        assertFalse(contextFactory.get(request).isDebuggingEnabled());
    }
    
    @Test
    public void assertThatContextFactorySetsDebuggingEnabledToRequestValue()
    {
        when(request.getParameter("debug")).thenReturn("true");
        assertTrue(contextFactory.get(request).isDebuggingEnabled());        
    }
    
    @Test
    public void assertThatContextFactorySetsDebuggingEnabledToSystemPropertyValue()
    {
        withSystemProperty(DEBUG_PROPERTY_KEY, "true", new Runnable()
        {
            public void run()
            {
                assertTrue(contextFactory.get(request).isDebuggingEnabled());
            }
        });
    }

    private void withSystemProperty(String property, String value, Runnable runnable)
    {
        String previousValue = System.getProperty(property);
        System.setProperty(property, value);
        try
        {
            runnable.run();
        }
        finally
        {
            if (previousValue == null)
            {
                System.getProperties().remove(property);
            }
            else
            {
                System.setProperty(property, previousValue);
            }
        }
    }
}

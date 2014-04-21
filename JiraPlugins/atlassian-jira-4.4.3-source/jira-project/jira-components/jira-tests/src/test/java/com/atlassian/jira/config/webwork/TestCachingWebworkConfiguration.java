package com.atlassian.jira.config.webwork;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import webwork.config.ConfigurationInterface;

/**
 */
public class TestCachingWebworkConfiguration extends MockControllerTestCase
{
    @Test
    public void testConstruction()
    {
        try
        {
            new CachingWebworkConfiguration(null);
            fail("This should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testWriteNotSupported()
    {
        mockController.getMock(ConfigurationInterface.class);

        CachingWebworkConfiguration cachingWebworkConfiguration = mockController.instantiate(CachingWebworkConfiguration.class);
        try
        {
            cachingWebworkConfiguration.setImpl("should", "go bang!");
            fail("This should have barfed");
        }
        catch (UnsupportedOperationException expected)
        {
        }
    }

    @Test
    public void testReadButNotCached()
    {
        final ConfigurationInterface delegate = mockController.getMock(ConfigurationInterface.class);
        delegate.getImpl("key1");
        mockController.setReturnValue("val1");

        delegate.getImpl("key1");
        mockController.setReturnValue("val2");

        CachingWebworkConfiguration cachingWebworkConfiguration = mockController.instantiate(CachingWebworkConfiguration.class);

        Object actualVal = cachingWebworkConfiguration.getImpl("key1");
        assertEquals("val1", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("key1");
        assertEquals("val2", actualVal);

    }

    @Test
    public void testReadAndCached()
    {
        final ConfigurationInterface delegate = mockController.getMock(ConfigurationInterface.class);
        delegate.getImpl("webwork.key1");
        mockController.setReturnValue("val1");

        delegate.getImpl("not.webwork.key1");
        mockController.setReturnValue("not.val1");

        CachingWebworkConfiguration cachingWebworkConfiguration = mockController.instantiate(CachingWebworkConfiguration.class);

        Object actualVal = cachingWebworkConfiguration.getImpl("webwork.key1");
        assertEquals("val1", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.key1");
        assertEquals("val1", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("not.webwork.key1");
        assertEquals("not.val1", actualVal);
    }

    @Test
    public void testUncacheableExceptions()
    {
        final ConfigurationInterface delegate = mockController.getMock(ConfigurationInterface.class);
        delegate.getImpl("webwork.multipart.maxSize");
        mockController.setReturnValue("val1");

        delegate.getImpl("webwork.multipart.maxSize");
        mockController.setReturnValue("val2");

        delegate.getImpl("webwork.i18n.encoding");
        mockController.setReturnValue("val3");

        delegate.getImpl("webwork.i18n.encoding");
        mockController.setReturnValue("val4");

        CachingWebworkConfiguration cachingWebworkConfiguration = mockController.instantiate(CachingWebworkConfiguration.class);

        Object actualVal = cachingWebworkConfiguration.getImpl("webwork.multipart.maxSize");
        assertEquals("val1", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.multipart.maxSize");
        assertEquals("val2", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.i18n.encoding");
        assertEquals("val3", actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.i18n.encoding");
        assertEquals("val4", actualVal);
    }

    @Test
    public void testWeOnlyCacheNonNullValues()
    {
        final ConfigurationInterface delegate = mockController.getMock(ConfigurationInterface.class);
        delegate.getImpl("webwork.key1");
        mockController.setReturnValue(null);

        delegate.getImpl("webwork.key1");
        mockController.setReturnValue(null);

        CachingWebworkConfiguration cachingWebworkConfiguration = mockController.instantiate(CachingWebworkConfiguration.class);

        Object actualVal = cachingWebworkConfiguration.getImpl("webwork.key1");
        assertNull(actualVal);

        actualVal = cachingWebworkConfiguration.getImpl("webwork.key1");
        assertNull(actualVal);
    }
}

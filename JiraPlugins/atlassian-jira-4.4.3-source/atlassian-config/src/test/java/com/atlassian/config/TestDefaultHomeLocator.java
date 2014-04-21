package com.atlassian.config;

import junit.framework.TestCase;

public class TestDefaultHomeLocator extends TestCase
{
    private String oldHomeProperty;
    protected DefaultHomeLocator homeLocator;
    protected static final String INIT_PROPERTY = "test.home";

    public void setUp() throws Exception
    {
        super.setUp();
        oldHomeProperty = System.getProperty(INIT_PROPERTY);
        if (oldHomeProperty != null)
            fail("Hey! The home property wasn't null! Is this leaking from somewhere? " + oldHomeProperty);

        System.getProperties().remove(INIT_PROPERTY);
        homeLocator = new DefaultHomeLocator();
        homeLocator.setInitPropertyName(INIT_PROPERTY);
    }

    public void tearDown() throws Exception
    {
        if (oldHomeProperty == null)
            System.getProperties().remove(INIT_PROPERTY);
        else
            System.setProperty(INIT_PROPERTY, oldHomeProperty);

        super.tearDown();
    }

    public void testGetHomePathFromConfigFile()
    {
        homeLocator.setPropertiesFile("test-homelocator-init.properties");
        assertEquals("c:/temp/conftest", homeLocator.getHomePath());
    }

    public void testSystemPropertyOverridesConfigFile()
    {
        homeLocator.setPropertiesFile("test-homelocator-init.properties");
        System.setProperty(INIT_PROPERTY, "/tmp/fish");
        assertEquals("/tmp/fish", homeLocator.getHomePath());
    }

    public void testNonExistentConfigFile()
    {
        homeLocator.setPropertiesFile("my-cats-breath-smells-like-cat-food.properties");
        assertNull(homeLocator.getHomePath());
    }
}

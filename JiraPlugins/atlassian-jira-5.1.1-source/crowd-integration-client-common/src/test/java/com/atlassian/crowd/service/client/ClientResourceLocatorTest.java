package com.atlassian.crowd.service.client;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * ClientResourceLocator Tester.
 */
public class ClientResourceLocatorTest
{
    private ClientResourceLocator clientResourceLocator = null;
    private String propertyFileName;

    @Before
    public void setUp() throws Exception
    {
        propertyFileName = ClientResourceLocatorTest.class.getSimpleName() + ".properties";
        clientResourceLocator = new ClientResourceLocator(propertyFileName);
    }

    @After
    public void tearDown() throws Exception
    {
        clientResourceLocator = null;
        propertyFileName = null;
    }

    @Test
    public void testGetResourceLocation() throws Exception
    {
        String resourceLocation = clientResourceLocator.getResourceLocation();
        Assert.assertNotNull(resourceLocation);
        Assert.assertTrue(resourceLocation.indexOf(propertyFileName) > 0);
    }

    @Test
    public void testGetResourceName()
    {
        String name = clientResourceLocator.getResourceName();

        Assert.assertEquals(propertyFileName, name);
    }

    @Test
    public void testFormatFileLocation() throws IOException
    {
        File tmpFile = new File("testfile.properties");
        FileUtils.writeStringToFile(tmpFile, "test=value", "UTF-8");
        tmpFile.deleteOnExit();

        String formattedLocation = clientResourceLocator.formatFileLocation(tmpFile.getAbsolutePath());
        Assert.assertNotNull(formattedLocation);
        Assert.assertTrue(formattedLocation.contains("file:"));
    }

    @Test
    public void testGetProperties()
    {
        Properties properties = clientResourceLocator.getProperties();
        Assert.assertTrue(properties.containsKey("application.name"));
    }

    @Test
    public void testGetPropertiesFromConfigurationDirectory() throws Exception
    {
        File tmpFile = File.createTempFile("crowd", ".properties");
        FileUtils.writeStringToFile(tmpFile, "test=value", "UTF-8");
        tmpFile.deleteOnExit();

        ClientResourceLocator clientResourceLocator = new ClientResourceLocator(tmpFile.getName(), tmpFile.getParent());
        Assert.assertEquals("value", clientResourceLocator.getProperties().get("test"));
    }
}

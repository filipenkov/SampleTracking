package com.atlassian.applinks.core.property;

import com.atlassian.applinks.api.PropertySet;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.refapp.sal.pluginsettings.RefimplPluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import junit.framework.TestCase;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationLinkPropertiesTest extends TestCase
{
    private static final URI CONF_URL;
    private static final URI LOCAL_URL;
    private static final TypeId CONF_TYPE_ID = new TypeId("confluence");
    private PluginSettings pluginSettings;
    private PropertySet adminApplinksPropertySet;
    private ApplicationLinkProperties applicationLinkProperties;
    private PropertySet customApplinksProperySet;

    static
    {
        try
        {
            CONF_URL = new URI("http://confluence");
            LOCAL_URL = new URI("http://192.168.10.1");
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setUp()
    {
        pluginSettings = new MockPluginSettingsPropertySet();
        adminApplinksPropertySet = new SalPropertySet(pluginSettings, ".general");
        customApplinksProperySet = new SalPropertySet(pluginSettings, ".custom");
        applicationLinkProperties = new ApplicationLinkProperties(adminApplinksPropertySet, customApplinksProperySet);
    }

    public void testSetProperties() throws Exception
    {
        applicationLinkProperties.setIsPrimary(true);
        applicationLinkProperties.setName("bob");
        applicationLinkProperties.setDisplayUrl(CONF_URL);
        applicationLinkProperties.setRpcUrl(LOCAL_URL);
        applicationLinkProperties.setType(CONF_TYPE_ID);

        assertEquals("bob", applicationLinkProperties.getName());
        assertEquals(CONF_URL, applicationLinkProperties.getDisplayUrl());
        assertEquals(LOCAL_URL, applicationLinkProperties.getRpcUrl());
        assertEquals(CONF_TYPE_ID, applicationLinkProperties.getType());
    }

    public void testDeleteProperties() throws Exception
    {
        applicationLinkProperties.setIsPrimary(true);
        applicationLinkProperties.setName("bob");
        applicationLinkProperties.setDisplayUrl(CONF_URL);
        applicationLinkProperties.setRpcUrl(LOCAL_URL);
        applicationLinkProperties.setType(CONF_TYPE_ID);

        assertEquals("bob", applicationLinkProperties.getName());
        assertEquals(CONF_URL, applicationLinkProperties.getDisplayUrl());
        assertEquals(LOCAL_URL, applicationLinkProperties.getRpcUrl());
        assertEquals(CONF_TYPE_ID, applicationLinkProperties.getType());
        assertEquals(true, applicationLinkProperties.isPrimary());
        assertEquals("true", adminApplinksPropertySet.getProperty(ApplicationLinkProperties.Property.PRIMARY.key()));

        applicationLinkProperties.remove();

        assertEquals(null, applicationLinkProperties.getName());
        assertEquals(null, applicationLinkProperties.getDisplayUrl());
        assertEquals(null, applicationLinkProperties.getRpcUrl());
        assertEquals(null, applicationLinkProperties.getType());
        assertEquals(null, adminApplinksPropertySet.getProperty(ApplicationLinkProperties.Property.PRIMARY.key()));
        assertEquals(false, applicationLinkProperties.isPrimary());
    }

    public void testSetAuthProviderConfig() throws Exception
    {
        applicationLinkProperties.setIsPrimary(true);
        applicationLinkProperties.setName("bob");
        applicationLinkProperties.setDisplayUrl(CONF_URL);
        applicationLinkProperties.setRpcUrl(LOCAL_URL);
        applicationLinkProperties.setType(CONF_TYPE_ID);

        final HashMap<String, String> config = new HashMap<String, String>();
        config.put("name", "myName");
        applicationLinkProperties.setProviderConfig("seraph", config);

        final Map<String, String> readConfig = applicationLinkProperties.getProviderConfig("seraph");
        assertEquals("myName", readConfig.get("name"));
        assertEquals("bob", applicationLinkProperties.getName());
    }

    public void testRemoveAuthProviderConfig() throws Exception
    {
        applicationLinkProperties.setIsPrimary(true);
        applicationLinkProperties.setName("bob");
        applicationLinkProperties.setDisplayUrl(CONF_URL);
        applicationLinkProperties.setRpcUrl(LOCAL_URL);
        applicationLinkProperties.setType(CONF_TYPE_ID);

        final HashMap<String, String> config = new HashMap<String, String>();
        config.put("name", "myName");
        applicationLinkProperties.setProviderConfig("seraph", config);

        final Map<String, String> readConfig = applicationLinkProperties.getProviderConfig("seraph");
        assertEquals("myName", readConfig.get("name"));
        assertEquals("bob", applicationLinkProperties.getName());

        applicationLinkProperties.removeProviderConfig("seraph");
        assertEquals(null, applicationLinkProperties.getProviderConfig("seraph"));
    }

    public void testRemoveApplicationLinkConfiguration() throws Exception
    {
        applicationLinkProperties.setIsPrimary(true);
        applicationLinkProperties.setName("bob");
        applicationLinkProperties.setDisplayUrl(CONF_URL);
        applicationLinkProperties.setRpcUrl(LOCAL_URL);
        applicationLinkProperties.setType(CONF_TYPE_ID);

        final HashMap<String, String> config = new HashMap<String, String>();
        config.put("name", "myName");
        applicationLinkProperties.setProviderConfig("seraph", config);

        final Map<String, String> readConfig = applicationLinkProperties.getProviderConfig("seraph");
        assertEquals("myName", readConfig.get("name"));
        assertEquals("bob", applicationLinkProperties.getName());

        applicationLinkProperties.remove();
        assertEquals(null, applicationLinkProperties.getProviderConfig("seraph"));
    }

    public void testSetCustomProperty() throws Exception
    {
        applicationLinkProperties.putProperty("name", "bob");
        assertEquals("bob", applicationLinkProperties.getProperty("name"));
    }

    public void testRemoveCustomProperty() throws Exception
    {
        applicationLinkProperties.putProperty("name", "bob");
        assertEquals("bob", applicationLinkProperties.getProperty("name"));
        final List<String> properties = (List<String>) adminApplinksPropertySet.getProperty("propertyKeys");
        assertTrue(properties.contains("name"));
        applicationLinkProperties.removeProperty("name");
        assertEquals(null, applicationLinkProperties.getProperty("name"));
        final List<String> remainingProperties = (List<String>) adminApplinksPropertySet.getProperty("propertyKeys");
        assertFalse(remainingProperties.contains("name"));
    }

}

package com.atlassian.crowd.service.client;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.atlassian.crowd.integration.Constants;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ClientPropertiesImplTest
{
    private Properties property;


    private ClientPropertiesImpl clientProperties;

    private String url1 = "http://localhost:8095/crowd";
    private String url2 = "http://localhost:8095/crowd/";
    private String url3 = "http://localhost:8095/crowd/services";
    private String url4 = "http://localhost:8095/crowd/services/";
    private String url5 = "http://localhost:8095/crowd/services/SecurityServer";
    private String url6 = "http://localhost:8095/crowd/services/SecurityServer/";
    private String url7 = "http://services:8095/crowd";
    private String url8 = "http://localhost:8095/services/crowd/services/SecurityServer/";

    private final String TARGET_BASE_URL = "http://localhost:8095/crowd";

    @Before
    public void setUp()
    {
        // Create the base property with all properties except "Constants.PROPERTIES_FILE_SECURITY_SERVER_URL"
        property = new Properties();
        property.setProperty(Constants.PROPERTIES_FILE_APPLICATION_NAME, "crowd");
        property.setProperty(Constants.PROPERTIES_FILE_APPLICATION_PASSWORD, "sDRkwFwX");
        property.setProperty(Constants.PROPERTIES_FILE_APPLICATION_LOGIN_URL, "http://localhost:8095/crowd/console/");
        property.setProperty(Constants.PROPERTIES_FILE_COOKIE_TOKENKEY, "session.tokenkey");
        property.setProperty(Constants.PROPERTIES_FILE_SESSIONKEY_VALIDATIONINTERVAL, "0");
        property.setProperty(Constants.PROPERTIES_FILE_SESSIONKEY_LASTVALIDATION, "session.lastvalidation");
        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, "http://garbage.example.com");

        clientProperties = ClientPropertiesImpl.newInstanceFromProperties(property);
    }

    @Test
    public void testGetBaseUrl()
    {
        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, url1);
        updateAndAssertProperty();

        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, url2);
        updateAndAssertProperty();

        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, url3);
        updateAndAssertProperty();

        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, url4);
        updateAndAssertProperty();

        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, url5);
        updateAndAssertProperty();

        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, url6);
        updateAndAssertProperty();

        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, url7);
        // we can't use the TARGET_BASE_URL here because we want to verify that having
        // services elsewhere in the URL is handled properly
        clientProperties.updateProperties(property);
        assertEquals(url7, clientProperties.getBaseURL());

        property.setProperty(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, url8);
        // we can't use the TARGET_BASE_URL here because we want to verify that having
        // services elsewhere in the URL is handled properly
        clientProperties.updateProperties(property);
        assertEquals("http://localhost:8095/services/crowd", clientProperties.getBaseURL());
    }

    private void updateAndAssertProperty()
    {
        clientProperties.updateProperties(property);
        final String url = clientProperties.getBaseURL();
        assertEquals(TARGET_BASE_URL, url);
    }

    @Test
    public void loadPropertyStringPrefersSystemProperties()
    {
        String propName = getClass().getName() + ".testProperty";

        ClientPropertiesImpl clientProps = new ClientPropertiesImpl();

        Properties props = new Properties();

        assertNull("Null is returned when no property value is set",
                clientProps.loadPropertyString(props, propName));

        props.setProperty(propName, "Value in Properties");
        assertEquals("Client properties are taken from the Properties object",
                "Value in Properties", clientProps.loadPropertyString(props, propName));

        String value = "Value in system property: " + Math.random();

        System.setProperty("crowd.property." + propName, value);
        assertEquals("A system property overrides the Properties contents",
                value, clientProps.loadPropertyString(props, propName));
    }

    /**
     * This test will <em>only run</em> if an environment variable called
     * <code>CROWD_PROPERTY_TEST_PROPERTY</code> is set.
     */
    @Test
    public void loadPropertyStringCanUseEnvironmentVariables()
    {
        String value = System.getenv("CROWD_PROPERTY_TEST_PROPERTY");
        Assume.assumeNotNull(value);

        ClientPropertiesImpl clientProps = new ClientPropertiesImpl();
        Properties props = new Properties();

        System.setProperty("atlassian.use.environment.variables", "");
        clientProps.updateProperties(props);
        assertNull(clientProps.loadPropertyString(props, "TEST_PROPERTY"));

        System.setProperty("atlassian.use.environment.variables", "false");
        clientProps.updateProperties(props);
        assertNull(clientProps.loadPropertyString(props, "TEST_PROPERTY"));

        System.setProperty("atlassian.use.environment.variables", "true");
        clientProps.updateProperties(props);
        assertEquals(value, clientProps.loadPropertyString(props, "TEST_PROPERTY"));
    }

    @Test
    public void loadPropertiesLeavesInvalidUrlsUnprocessed() throws Exception
    {
        String invalidUrl = "%not-a-valid-url";

        /* Confirm that it's invalid before using it */
        try
        {
            new URI(invalidUrl);
            fail();
        }
        catch (URISyntaxException e)
        {
        }

        Properties props = new Properties();
        props.put(Constants.PROPERTIES_FILE_SECURITY_SERVER_URL, invalidUrl);

        ClientPropertiesImpl clientProps = new ClientPropertiesImpl();
        clientProps.updateProperties(props);

        assertEquals(invalidUrl, clientProps.getBaseURL());
    }
}

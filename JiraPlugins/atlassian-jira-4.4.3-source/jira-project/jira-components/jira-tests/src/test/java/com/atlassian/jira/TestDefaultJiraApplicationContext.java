package com.atlassian.jira;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Tests the DefaultJiraApplicationContext class.
 *
 * @since v3.13
 */
public class TestDefaultJiraApplicationContext extends MockControllerTestCase
{
    private ApplicationProperties applicationProperties;
    private JiraLicenseService jiraLicenseService;

    @Before
    public void setUp() throws Exception
    {
        jiraLicenseService = getMock(JiraLicenseService.class);
        applicationProperties = getMock(ApplicationProperties.class);
        replay();
    }

    @Test
    public void testGetFingerPrintHappy() {
        // we know the application properties are only used by the getApplicationProperties instance method.
        JiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService) {
            String getServerId()
            {
                return "12345";
            }

            String getBaseUrl()
            {
                return "http://myjira.com/";
            }

            String generateFingerPrint(String serverId, String baseUrl)
            {
                return serverId + baseUrl;
            }
        };

        assertEquals("12345http://myjira.com/", applicationContext.getFingerPrint());
    }

    @Test
    public void testGetFingerPrintNullBaseUrl() {
        // we know the application properties are only used by the getApplicationProperties instance method.
        JiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService) {
            String getServerId()
            {
                return "12345";
            }

            String getBaseUrl()
            {
                return null;
            }

            String generateFingerPrint(String serverId, String baseUrl)
            {
                return serverId + baseUrl;
            }
        };

        assertEquals("12345null", applicationContext.getFingerPrint());
    }

    @Test
    public void testGetFingerPrintNullServerId() {
        JiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService) {
            String getServerId()
            {
                return null;
            }

            String getBaseUrl()
            {
                return "http://foobar.com/jira";
            }

            String getTemporaryServerId()
            {
                return "temporary";
            }

            String generateFingerPrint(String serverId, String baseUrl)
            {
                return serverId + baseUrl;
            }
        };
        assertEquals("temporaryhttp://foobar.com/jira", applicationContext.getFingerPrint());
    }

    @Test
    public void testGenerateFingerPrint() {
        DefaultJiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService);
        String fingerPrint = applicationContext.generateFingerPrint("foo", "bar");
        assertEquals(DigestUtils.md5Hex("foobar"), fingerPrint);
    }

    @Test
    public void testGetTemporaryServerIdNotNull() {
        DefaultJiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService);
        String tempId = applicationContext.getTemporaryServerId();
        assertNotNull(tempId);
    }
}

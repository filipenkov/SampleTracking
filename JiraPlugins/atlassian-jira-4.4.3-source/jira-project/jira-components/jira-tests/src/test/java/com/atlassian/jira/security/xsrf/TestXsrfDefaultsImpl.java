package com.atlassian.jira.security.xsrf;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.config.properties.APKeys;

/**
 * @since v4.0
 */
public class TestXsrfDefaultsImpl extends ListeningTestCase
{

    @Test
    public void testValuesMissingAndHenceJIRADefaults()
    {
        MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();

        XsrfDefaults defaults = new XsrfDefaultsImpl(mockApplicationProperties);
        assertTrue(defaults.isXsrfProtectionEnabled());
    }

    @Test
    public void testAllFalse()
    {
        MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        mockApplicationProperties.setString(APKeys.JIRA_XSRF_ENABLED, "false");

        XsrfDefaults defaults = new XsrfDefaultsImpl(mockApplicationProperties);
        assertFalse(defaults.isXsrfProtectionEnabled());
    }

    @Test
    public void testRubbishValues()
    {
        MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        mockApplicationProperties.setString(APKeys.JIRA_XSRF_ENABLED, "flurg");

        XsrfDefaults defaults = new XsrfDefaultsImpl(mockApplicationProperties);
        assertFalse(defaults.isXsrfProtectionEnabled());
    }

    @Test
    public void testAllTrue()
    {
        MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        mockApplicationProperties.setString(APKeys.JIRA_XSRF_ENABLED, "true");

        XsrfDefaults defaults = new XsrfDefaultsImpl(mockApplicationProperties);
        assertTrue(defaults.isXsrfProtectionEnabled());
    }
}

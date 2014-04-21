package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test {@link com.atlassian.jira.upgrade.tasks.UpgradeTask_Build317} sets the default
 * value for {@link com.atlassian.jira.config.properties.APKeys#JIRA_ATTACHMENT_SIZE}
 * property if no value has been set.
 */
public class TestUpgradeTask_Build317 extends ListeningTestCase
{
    private UpgradeTask_Build317_TestWrapper ut;
    private ApplicationProperties ap;

    @Before
    public void setUp() throws Exception
    {
        ap = new MockApplicationProperties();
        ut = new UpgradeTask_Build317_TestWrapper();
    }

    @After
    public void tearDown() throws Exception
    {
        ap = null;
        ut = null;
    }

    /**
     * Test that short description is set
     */
    @Test
    public void testShortDescription()
    {
        assertNotNull(ut.getShortDescription());
    }

    /**
     * Test the build number is 298
     */
    @Test
    public void testBuildNumber()
    {
        String buildNumber = ut.getBuildNumber();
        assertNotNull(buildNumber);
        assertEquals("317", buildNumber);
    }

    @Test
    public void testDoUpgradeWithNoProperties() throws Exception
    {
        //test null webwork properties (ie. could not load)
        ut = new UpgradeTask_Build317_TestWrapper()
        {
            protected Properties getWebworkProperties()
            {
                return null;
            }
        };
        assertFalse(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals("10485760", ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));
    }

    /**
     * Test that upgrade task adds the {@link com.atlassian.jira.config.properties.APKeys#JIRA_ATTACHMENT_SIZE}
     * @throws Exception on error
     */
    @Test
    public void testDoUpgrade() throws Exception
    {
        final String defaultValue = "10485760";

        //initially no entry
        assertFalse(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        ut.setMaxSize(null);
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals(defaultValue, ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        //add invalid value string
        ut.setMaxSize("something");
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals(defaultValue, ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        //add invalid null
        ut.setMaxSize(null);
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals(defaultValue, ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        //add invalid "" empty string
        ut.setMaxSize("");
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals(defaultValue, ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        //value is set to a integer
        ut.setMaxSize("12345");
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals("12345", ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        //test max value edge case (ie. value == Integer.MAX_VALUE)
        ut.setMaxSize("2147483647");
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals("2147483647", ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        //value is too large (ie. value > Integer.MAX_VALUE)
        ut.setMaxSize("2147483648");
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals(defaultValue, ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        //value is negative (ie. value < 0)
        ut.setMaxSize("-1");
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals(defaultValue, ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));

        //value is zero (ie. value == 0)
        ut.setMaxSize("0");
        ut.doUpgrade(false);
        assertTrue(StringUtils.isNotEmpty(ap.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)));
        assertEquals("0", ap.getString(APKeys.JIRA_ATTACHMENT_SIZE));
    }

    class UpgradeTask_Build317_TestWrapper extends UpgradeTask_Build317
    {
        private String maxSize;

        protected ApplicationProperties getApplicationProperties()
        {
            return ap;
        }

        protected Properties getWebworkProperties()
        {
            Properties properties = new Properties();
            if (maxSize != null)
            {
                properties.put(APKeys.JIRA_ATTACHMENT_SIZE, maxSize);
            }
            return properties;
        }

        public void setMaxSize(String maxSize)
        {
            this.maxSize = maxSize;
        }
    }
}

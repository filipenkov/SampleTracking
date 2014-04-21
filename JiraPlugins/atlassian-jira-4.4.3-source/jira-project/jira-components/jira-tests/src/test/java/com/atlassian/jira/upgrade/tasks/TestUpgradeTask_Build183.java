package com.atlassian.jira.upgrade.tasks;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;

public class TestUpgradeTask_Build183 extends ListeningTestCase
{

    private UpgradeTask_Build183 ut;
    private ApplicationProperties ap;

    @Before
    public void setUp() throws Exception
    {
        ap = new MockApplicationProperties();
        ut = new UpgradeTask_Build183()
        {
            protected ApplicationProperties getApplicationProperties()
            {
                return ap;
            }
        };
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
     * Test the build number is 183
     */
    @Test
    public void testBuildNumber()
    {
        String buildNumber = ut.getBuildNumber();
        assertNotNull(buildNumber);
        assertEquals("183", buildNumber);
    }

    /**
     * Test that upgrade task 183 no longer does anything. Superceded by Upgrade task 187
     */
    @Test
    public void testDoUpgrade()
    {
        ut.doUpgrade(false);
        assertEquals(null, ap.getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        ap.setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, null);
        ut.doUpgrade(false);
        assertFalse(ap.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
        assertEquals(null, ap.getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        ap.setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, "something");
        ut.doUpgrade(false);
        assertTrue(ap.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
        assertEquals("something", ap.getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        ap.setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, "");
        ut.doUpgrade(false);
        assertTrue(ap.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
        assertEquals("", ap.getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
    }

}

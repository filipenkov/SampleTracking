package com.atlassian.jira.upgrade.tasks;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.mock.MockApplicationProperties;

public class TestUpgradeTask_Build187 extends ListeningTestCase
{
    private UpgradeTask_Build187 ut;
    private ApplicationProperties ap;

    @Before
    public void setUp() throws Exception
    {
        ap = new MockApplicationProperties();
        ut = new UpgradeTask_Build187()
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
     * Test the build number is 205
     */
    @Test
    public void testBuildNumber()
    {
        String buildNumber = ut.getBuildNumber();
        assertNotNull(buildNumber);
        assertEquals("187", buildNumber);
    }

    /**
     * Test that upgrade task removes the jira.trackback.exclude.pattern if it is null or an empty string.
     */
    @Test
    public void testDoUpgrade()
    {
        //initially no entry
        assertFalse(ap.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
        ut.doUpgrade(false);
        assertFalse(ap.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        //add valid string
        ap.setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, "something");
        ut.doUpgrade(false);
        assertTrue(ap.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
        assertEquals("something", ap.getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        //add invalid null
        ap.setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, null);
        ut.doUpgrade(false);
        assertFalse(ap.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        //add invalid "" empty string
        ap.setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, "");
        ut.doUpgrade(false);
        assertFalse(ap.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
    }
}

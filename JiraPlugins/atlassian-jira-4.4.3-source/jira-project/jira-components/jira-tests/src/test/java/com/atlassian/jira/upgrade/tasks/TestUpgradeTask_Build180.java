package com.atlassian.jira.upgrade.tasks;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.mock.MockApplicationProperties;

public class TestUpgradeTask_Build180 extends ListeningTestCase
{

    private UpgradeTask_Build180 ut;
    private ApplicationProperties ap;

    @Before
    public void setUp() throws Exception
    {
        ap = new MockApplicationProperties();
        ut = new UpgradeTask_Build180()
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
     * Test the build number is 180
     */
    @Test
    public void testBuildNumber()
    {
        String buildNumber = ut.getBuildNumber();
        assertNotNull(buildNumber);
        assertEquals("180", buildNumber);
    }

    /**
     * Test that upgrade task sets group visibility to true
     */
    @Test
    public void testDoUpgrade()
    {
        ap.setOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS, false);
        ut.doUpgrade(false);
        assertTrue(ap.getOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS));

        ap.setOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS, true);
        ut.doUpgrade(false);
        assertTrue(ap.getOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS));

        ap.setOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS, false);
        ut.doUpgrade(false);
        assertTrue(ap.getOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS));
    }

}

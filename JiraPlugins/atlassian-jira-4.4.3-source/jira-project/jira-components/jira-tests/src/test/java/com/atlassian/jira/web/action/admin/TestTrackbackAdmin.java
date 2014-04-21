package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.mock.MockApplicationProperties;

public class TestTrackbackAdmin extends ListeningTestCase
{
    private MockApplicationProperties applicationProperties;
    private TrackbackAdmin trackbackAdmin;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = new MockApplicationProperties();
        trackbackAdmin = new TrackbackAdmin(null, null)
        {
            public ApplicationProperties getApplicationProperties()
            {
                return applicationProperties;
            }
        };
    }

    @Test
    public void testTrackbackAdminSetTrackbackUrl()
    {
        //initially no entry
        assertFalse(applicationProperties.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        //add valid string
        trackbackAdmin.setUrlExcludePattern("something");
        assertTrue(applicationProperties.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
        assertEquals("something", applicationProperties.getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        //add invalid null
        trackbackAdmin.setUrlExcludePattern(null);
        assertFalse(applicationProperties.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));

        //add invalid "" empty string
        trackbackAdmin.setUrlExcludePattern("");
        assertFalse(applicationProperties.exists(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN));
    }
}

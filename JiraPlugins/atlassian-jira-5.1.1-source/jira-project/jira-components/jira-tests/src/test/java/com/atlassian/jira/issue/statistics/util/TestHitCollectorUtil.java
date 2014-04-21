package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @since v4.0
 */
public class TestHitCollectorUtil extends ListeningTestCase
{
    @Test
    public void testGetFieldId() throws Exception
    {
        HitCollectorUtil hitCollectorUtil = new HitCollectorUtil();
        assertEquals("customfield_10000", hitCollectorUtil.getFieldId("customfield_10000_blah_blee"));
        assertEquals("customfield_10000", hitCollectorUtil.getFieldId("customfield_10000"));
        assertEquals("customfield_10000", hitCollectorUtil.getFieldId("customfield_10000SDFDSFSDFSDF"));
        assertEquals("fixVersions", hitCollectorUtil.getFieldId("fixfor"));
    }
}

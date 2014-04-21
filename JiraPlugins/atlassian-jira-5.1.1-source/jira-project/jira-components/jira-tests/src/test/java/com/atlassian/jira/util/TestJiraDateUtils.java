package com.atlassian.jira.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class TestJiraDateUtils extends ListeningTestCase
{

    @Test
    public void testCopyDateNullsafe() {

        // test that null parameter returns null
        assertNull(JiraDateUtils.copyDateNullsafe(null));

        // test that passing date returns a copy od that date
        Date originalDate = new Date();
        Date copyDate = JiraDateUtils.copyDateNullsafe(originalDate);
        assertNotNull(copyDate);
        assertEquals(originalDate, copyDate);
        assertNotSame(originalDate, copyDate);
    }

    @Test
    public void testCopyOrCreateDateNullsafe() {

        // test that null parameter returns null
        assertNotNull(JiraDateUtils.copyOrCreateDateNullsafe(null));

        // test that passing date returns a copy od that date
        Date originalDate = new Date();
        Date copyDate = JiraDateUtils.copyOrCreateDateNullsafe(originalDate);
        assertNotNull(copyDate);
        assertEquals(originalDate, copyDate);
        assertNotSame(originalDate, copyDate);
    }

}

package com.atlassian.jira.external.beans;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class TestExternalWorklog extends ListeningTestCase
{
    @Test
    public void testCreated() throws Exception
    {
        ExternalWorklog externalWorklog = new ExternalWorklog();

        // initially null
        assertNull(externalWorklog.getCreated());

        Date now = new Date();
        externalWorklog.setCreated(now);

        Date retNow = externalWorklog.getCreated();
        assertNotSame(now, retNow);
        assertEquals(now, retNow);

        // now in past
        now.setTime(System.currentTimeMillis() - 20000);
        Date retStillNow = externalWorklog.getCreated();
        assertNotSame(now, retStillNow);
        assertFalse(now.equals(retNow));
    }

    @Test
    public void testStartDate() throws Exception
    {
        ExternalWorklog externalWorklog = new ExternalWorklog();

        // initially null
        assertNull(externalWorklog.getStartDate());

        Date now = new Date();
        externalWorklog.setStartDate(now);

        Date retNow = externalWorklog.getStartDate();
        assertNotSame(now, retNow);
        assertEquals(now, retNow);

        // now in past
        now.setTime(System.currentTimeMillis() - 20000);
        Date retStillNow = externalWorklog.getStartDate();
        assertNotSame(now, retStillNow);
        assertFalse(now.equals(retNow));
    }

    @Test
    public void testUpdated() throws Exception
    {
        ExternalWorklog externalWorklog = new ExternalWorklog();

        // initially null
        assertNull(externalWorklog.getUpdated());

        Date now = new Date();
        externalWorklog.setUpdated(now);

        Date retNow = externalWorklog.getUpdated();
        assertNotSame(now, retNow);
        assertEquals(now, retNow);

        // now in past
        now.setTime(System.currentTimeMillis() - 20000);
        Date retStillNow = externalWorklog.getUpdated();
        assertNotSame(now, retStillNow);
        assertFalse(now.equals(retNow));
    }

}

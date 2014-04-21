package com.atlassian.jira.issue.worklog;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

/**
 *
 */
public class TestWorklogImpl extends ListeningTestCase
{
    private static final String CREATE_AUTHOR = "test create author";
    private static final Long TIME_SPENT = new Long(1000);
    private static final Date TIME_PERFORMED = new Date(20000);
    private static final Date UPDATED_DATE = new Date(34343);
    private static final Date CREATED_DATE = new Date(123456);
    private static final String UPDATED_AUTHOR = "updated author";

    @Test
    public void testShortConstructorHappyPath()
    {
        WorklogImpl worklog = new WorklogImpl(null, null, null, CREATE_AUTHOR, null, TIME_PERFORMED, null, null, TIME_SPENT);
        assertEquals(CREATE_AUTHOR, worklog.getAuthor());
        assertEquals(CREATE_AUTHOR, worklog.getUpdateAuthor());
        assertNotNull(worklog.getCreated());
        assertEquals(worklog.getCreated(), worklog.getUpdated());
        assertEquals(TIME_PERFORMED, worklog.getStartDate());
    }

    @Test
    public void testShortConstructorNoStartDate()
    {
        WorklogImpl worklog = new WorklogImpl(null, null, null, CREATE_AUTHOR, null, null, null, null, TIME_SPENT);
        assertEquals(CREATE_AUTHOR, worklog.getAuthor());
        assertEquals(CREATE_AUTHOR, worklog.getUpdateAuthor());
        assertNotNull(worklog.getCreated());
        assertEquals(worklog.getCreated(), worklog.getUpdated());
        assertEquals(worklog.getCreated(), worklog.getStartDate());
    }

    @Test
    public void testShortConstructorNoTimeSpent()
    {
        try
        {
            WorklogImpl worklog = new WorklogImpl(null, null, null, CREATE_AUTHOR, null, TIME_PERFORMED, null, null, null);
            fail("Should have thrown IllegalArgumentException if no timeSpent specified");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testLongConstructorHappyPath()
    {
        WorklogImpl worklog = new WorklogImpl(null, null, null, CREATE_AUTHOR, null, TIME_PERFORMED, null, null, TIME_SPENT, UPDATED_AUTHOR, CREATED_DATE, UPDATED_DATE);
        assertEquals(CREATE_AUTHOR, worklog.getAuthor());
        assertEquals(UPDATED_AUTHOR, worklog.getUpdateAuthor());
        assertEquals(CREATED_DATE, worklog.getCreated());
        assertEquals(UPDATED_DATE, worklog.getUpdated());
        assertEquals(TIME_PERFORMED, worklog.getStartDate());
    }
}

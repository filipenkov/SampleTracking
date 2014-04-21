package com.atlassian.jira.web.action.admin.index;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.local.ListeningTestCase;

/*
 * @since v3.13
 */

public class TestIndexCommandResult extends ListeningTestCase
{
    private static final int REINDEX_TIME = 50;
    private static final String ERRROR_MESSAGE = "ERRROR MESSAGE";

    @Test
    public void testTestIsOK() throws Exception
    {
        IndexCommandResult result = new IndexCommandResult(REINDEX_TIME);
        assertTrue(result.isSuccessful());

        SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessage(ERRROR_MESSAGE);

        result = new IndexCommandResult(collection);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testConstructor() throws Exception
    {
        IndexCommandResult result = new IndexCommandResult(REINDEX_TIME);
        assertEquals(REINDEX_TIME, result.getReindexTime());
        assertNotNull(result.getErrorCollection());
        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertTrue(result.isSuccessful());


        SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessage(ERRROR_MESSAGE);
        result = new IndexCommandResult(collection);
        assertEquals(0, result.getReindexTime());
        assertFalse(result.isSuccessful());
        assertEquals(collection, result.getErrorCollection());

        try
        {
            new IndexCommandResult(null);
            fail("Should not accept null collection.");
        }
        catch (RuntimeException e)
        {
            //expected
        }

    }
}

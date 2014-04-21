package com.atlassian.jira.web.action.admin.index;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/** @since v3.13 */
public class TestIndexTaskContext extends ListeningTestCase
{
    @Test
    public void testBuildProgressURL()
    {
        IndexTaskContext ctx = new IndexTaskContext();
        assertNotNull(ctx.buildProgressURL(new Long(6)));
        assertTrue(ctx.buildProgressURL(new Long(7)).startsWith("/"));
    }

    @Test
    public void testEquals()
    {
        IndexTaskContext ctx1 = new IndexTaskContext();
        IndexTaskContext ctx2 = new IndexTaskContext();

        assertFalse(ctx1.equals(null));
        assertTrue(ctx1.equals(ctx2));
        assertTrue(ctx1.equals(ctx1));

        assertEquals(ctx1.hashCode(), ctx2.hashCode());
    }
}

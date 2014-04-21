package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.workflow.migration.EnterpriseWorkflowTaskContext;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v3.13
 */
public class TestEnterpriseWorkflowTaskContext extends ListeningTestCase
{
    private static final Long PROJECT_ID1 = new Long(5);
    private static final Long SCHEME_ID1 = new Long(5000);
    private static final Long PROJECT_ID2 = new Long(6);

    @Test
    public void testBuildProgressConstruction()
    {
        try
        {
            new EnterpriseWorkflowTaskContext(null, PROJECT_ID1);
            fail("Object should not accept a null workflow.");
        }
        catch (RuntimeException e)
        {
            //expected.
        }

        EnterpriseWorkflowTaskContext ctx = new EnterpriseWorkflowTaskContext(PROJECT_ID1, null);
        assertEquals(PROJECT_ID1, ctx.getProjectId());
        assertNull(ctx.getSchemeId());

        ctx = new EnterpriseWorkflowTaskContext(PROJECT_ID1, SCHEME_ID1);
        assertEquals(PROJECT_ID1, ctx.getProjectId());
        assertEquals(SCHEME_ID1, ctx.getSchemeId());
    }

    @Test
    public void testBuildProgressURL()
    {
        EnterpriseWorkflowTaskContext ctx = new EnterpriseWorkflowTaskContext(PROJECT_ID1, SCHEME_ID1);
        assertNotNull(ctx.buildProgressURL(new Long(6)));
        assertTrue(ctx.buildProgressURL(new Long(-1)).startsWith("/"));

        ctx = new EnterpriseWorkflowTaskContext(PROJECT_ID1, null);
        assertNotNull(ctx.buildProgressURL(new Long(6)));
        assertTrue(ctx.buildProgressURL(new Long(-1)).startsWith("/"));
    }

    @Test
    public void testEquals()
    {
        EnterpriseWorkflowTaskContext ctx1 = new EnterpriseWorkflowTaskContext(PROJECT_ID1, SCHEME_ID1);
        EnterpriseWorkflowTaskContext ctx2 = new EnterpriseWorkflowTaskContext(PROJECT_ID1, null);
        EnterpriseWorkflowTaskContext ctx3 = new EnterpriseWorkflowTaskContext(PROJECT_ID2, null);

        assertFalse(ctx1.equals(null));
        assertTrue(ctx1.equals(ctx2));
        assertTrue(ctx1.equals(ctx1));

        assertFalse(ctx1.equals(ctx3));
                
        assertEquals(ctx1.hashCode(), ctx2.hashCode());
    }
}

package com.atlassian.jira.bc.project.component;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

public class TestProjectComponentImpl extends ListeningTestCase
{
    private ProjectComponent pc1;
    private ProjectComponent pc2;
    private ProjectComponent pc3;
    private Long projectId;

    @Before
    public void setUp() throws Exception
    {
        projectId = new Long(4);
        pc1 = new ProjectComponentImpl(new Long(123), "name", "desc", "lead", 0, projectId, null);
        pc2 = new ProjectComponentImpl(new Long(222), "name", "desc", "lead", 0, projectId, null);
        pc3 = new ProjectComponentImpl(new Long(123), "long name", null, null, 0, projectId, null);
    }

    @Test
    public void testEquals()
    {

        assertFalse(pc1.equals(null));
        assertFalse(pc1.equals("text"));

        assertTrue(pc1.equals(pc1));
        assertFalse(pc1.equals(pc2));
        assertTrue(pc1.equals(pc3));

        assertFalse(pc2.equals(pc1));
        assertTrue(pc2.equals(pc2));
        assertFalse(pc2.equals(pc3));

        assertTrue(pc3.equals(pc1));
        assertFalse(pc3.equals(pc2));
        assertTrue(pc3.equals(pc3));
    }

    @Test
    public void testHashCode()
    {
        assertTrue(pc1.hashCode() == pc1.hashCode());
        assertTrue(pc1.hashCode() != pc2.hashCode());
        assertTrue(pc1.hashCode() == pc3.hashCode());
        assertTrue(pc2.hashCode() == pc2.hashCode());
        assertTrue(pc2.hashCode() != pc3.hashCode());
        assertTrue(pc3.hashCode() == pc3.hashCode());
    }

    @Test
    public void testGetters()
    {
        assertEquals(new Long(123), pc1.getId());
        assertEquals("name", pc1.getName());
        assertEquals("desc", pc1.getDescription());
        assertEquals("lead", pc1.getLead());

        assertEquals(new Long(222), pc2.getId());
        assertEquals("name", pc2.getName());
        assertEquals("desc", pc2.getDescription());
        assertEquals("lead", pc2.getLead());

        assertEquals(new Long(123), pc3.getId());
        assertEquals("long name", pc3.getName());
        assertNull(pc3.getDescription());
        assertNull(pc3.getLead());
    }

    @Test
    public void testToString()
    {
        assertNotNull(pc1.toString());
        assertNotNull(pc2.toString());
        assertNotNull(pc3.toString());
        assertNotNull(new ProjectComponentImpl(null, null, null, null, 0, null, null).toString());
    }

}

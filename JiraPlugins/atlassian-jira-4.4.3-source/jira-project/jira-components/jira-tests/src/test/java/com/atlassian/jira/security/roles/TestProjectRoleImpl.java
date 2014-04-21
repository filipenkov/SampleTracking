package com.atlassian.jira.security.roles;
/**
 * Copyright All Rights Reserved.
 * Created: christo 24/07/2006 14:56:31
 */

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

public class TestProjectRoleImpl extends ListeningTestCase
{
    ProjectRoleImpl projectRoleImpl;

    /**
     * Tests the ProjectRoleComparator.COMPARATOR
     * @throws Exception at the drop of a hat.
     */
    @Test
    public void testProjectRoleComparator() throws Exception
    {
        ProjectRole aardvark = new ProjectRoleImpl("aardvark", "weird animal");
        ProjectRole aardvark2 = new ProjectRoleImpl("AArdvark2", "weird animal");
        ProjectRole administrator = new ProjectRoleImpl("Administrator", "weirder animal");
        ProjectRole utensil = new ProjectRoleImpl("utensil", "thing with which to eat");

        SortedSet roles  = new TreeSet(ProjectRoleComparator.COMPARATOR);

        // mix things up in the insertion order
        roles.add(administrator);
        roles.add(utensil);
        roles.add(aardvark2);
        roles.add(utensil); // deliberate duplication
        roles.add(aardvark);

        Iterator i = roles.iterator();

        assertEquals(aardvark, i.next());
        assertEquals(aardvark2, i.next());
        assertEquals(administrator, i.next());
        assertEquals(utensil, i.next());
        assertFalse(i.hasNext());
    }
}

/*
 * Atlassian Source Code Template.
 * User: Administrator
 * Created: Oct 16, 2002
 * Time: 7:12:21 PM
 * CVS Revision: $Revision: 1.1 $
 * Last CVS Commit: $Date: 2003/09/30 07:11:38 $
 * Author of last CVS Commit: $Author: mcannon $
 */
package com.atlassian.core.ofbiz.comparator;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.ofbiz.comparators.OFBizFieldComparator;
import com.atlassian.core.ofbiz.test.UtilsForTests;

import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import com.atlassian.jira.local.ListeningTestCase;

public class TestOFBizFieldComparator extends ListeningTestCase
{
    @Before
    public void setUp() throws Exception
    {
        UtilsForTests.cleanOFBiz();
    }

    @Test
    public void testComparison()
    {
        final OFBizFieldComparator comp = new OFBizFieldComparator("foo");

        assertTrue(0 == comp.compare(null, null));

        GenericValue gv = new MockGenericValue("Issue", UtilMisc.toMap("foo", null));
        assertTrue(comp.compare(gv, gv) == 0);
        assertTrue(comp.compare(gv, null) < 0);
        assertTrue(comp.compare(null, gv) > 0);

        GenericValue gv2 = new MockGenericValue("Issue", UtilMisc.toMap("foo", null));
        assertTrue(0 == comp.compare(gv, gv2));
        assertTrue(0 == comp.compare(gv2, gv));

        gv = new MockGenericValue("Issue", UtilMisc.toMap("foo", "value"));
        assertTrue(comp.compare(gv, gv2) < 0);
        assertTrue(comp.compare(gv2, gv) > 0);

        gv2 = new MockGenericValue("Issue", UtilMisc.toMap("foo", "a value"));
        assertTrue(comp.compare(gv, gv2) > 0);
        assertTrue(comp.compare(gv2, gv) < 0);
    }
}

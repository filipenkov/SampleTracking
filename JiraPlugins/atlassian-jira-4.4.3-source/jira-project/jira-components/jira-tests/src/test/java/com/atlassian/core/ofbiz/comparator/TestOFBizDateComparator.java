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

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.ofbiz.comparators.OFBizDateComparator;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.sql.Timestamp;
import java.util.Date;

public class TestOFBizDateComparator extends ListeningTestCase
{
    @Before
    public void setUp() throws Exception
    {
        UtilsForTests.cleanOFBiz();
    }

    @Test
    public void testComparison()
    {
        final OFBizDateComparator comp = new OFBizDateComparator("foo");

        GenericValue gv = new MockGenericValue("Issue", UtilMisc.toMap("foo", null));
        GenericValue gv2 = new MockGenericValue("Issue", UtilMisc.toMap("foo", null));
        assertTrue(0 == comp.compare(gv, gv2));
        assertTrue(0 == comp.compare(gv2, gv));

        gv = new MockGenericValue("Issue", UtilMisc.toMap("foo", new Timestamp(new Date().getTime())));
        assertTrue(comp.compare(gv, gv2) > 0);
        assertTrue(comp.compare(gv2, gv) < 0);

        gv2 = new MockGenericValue("Issue", UtilMisc.toMap("foo", new Timestamp(new Date().getTime() + 1000)));
        assertTrue(comp.compare(gv, gv2) < 0);
        assertTrue(comp.compare(gv2, gv) > 0);
    }

}

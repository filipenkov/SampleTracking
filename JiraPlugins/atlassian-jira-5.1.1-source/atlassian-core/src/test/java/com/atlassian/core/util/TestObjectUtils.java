/*
 * Atlassian Source Code Template.
 * User: owen
 * Date: Oct 17, 2002
 * Time: 2:00:14 PM
 * CVS Revision: $Revision: 1.5 $
 * Last CVS Commit: $Date: 2003/09/30 07:05:53 $
 * Author of last CVS Commit: $Author: mcannon $
 */
package com.atlassian.core.util;

import com.atlassian.core.util.ObjectUtils;
import junit.framework.TestCase;

public class TestObjectUtils extends TestCase
{
    public TestObjectUtils(String s)
    {
        super(s);
    }

    public void testIsDifferent()
    {
        assertTrue(ObjectUtils.isDifferent(new Integer(1), new Integer(2)));
        assertTrue(ObjectUtils.isDifferent(new Integer(2), new Integer(1)));
        assertTrue(ObjectUtils.isDifferent(new Integer(1), null));
        assertTrue(ObjectUtils.isDifferent(null, new Integer(1)));

        assertTrue(!(ObjectUtils.isDifferent(null, null)));
        assertTrue(!(ObjectUtils.isDifferent(new Integer(1), new Integer(1))));
    }

    public void testIsIdentical()
    {
        assertTrue(!ObjectUtils.isIdentical(new Integer(1), new Integer(2)));
        assertTrue(!ObjectUtils.isIdentical(new Integer(2), new Integer(1)));
        assertTrue(!ObjectUtils.isIdentical(new Integer(1), null));
        assertTrue(!ObjectUtils.isIdentical(null, new Integer(1)));

        assertTrue(ObjectUtils.isIdentical(null, null));
        assertTrue(ObjectUtils.isIdentical(new Integer(1), new Integer(1)));
    }
}

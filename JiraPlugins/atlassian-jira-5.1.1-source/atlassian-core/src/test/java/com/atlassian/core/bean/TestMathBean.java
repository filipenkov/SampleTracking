/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Jan 30, 2003
 * Time: 10:33:21 AM
 * CVS Revision: $Revision: 1.5 $
 * Last CVS Commit: $Date: 2004/01/19 03:00:43 $
 * Author of last CVS Commit: $Author: sfarquhar $
 * To change this template use Options | File Templates.
 */
package com.atlassian.core.bean;

import com.atlassian.core.bean.MathBean;
import junit.framework.TestCase;

public class TestMathBean extends TestCase
{
    private MathBean mb = new MathBean();

    public TestMathBean(String s)
    {
        super(s);
    }

    public void testPercentageWidth()
    {
        assertEquals(33, mb.getPercentageWidth(3, 1));
        assertEquals(33, mb.getPercentageWidth(3, 2));
        assertEquals(34, mb.getPercentageWidth(3, 3));
    }

    public void testAdd()
    {
        assertEquals(100, mb.add(45, 55));
    }

    public void testSubstract()
    {
        assertEquals(5, mb.subtract(55, 50));
    }

    public void testMultiply()
    {
        assertEquals(144, mb.multiply(12, 12));
    }

    public void testDivide()
    {
        assertEquals(33, mb.divide(100, 3));
    }

    public void testLongDivide()
    {
        assertEquals(33, mb.divide((long) 100, (long) 3));
    }

    public void testLongDivide2()
    {
        assertEquals(0, mb.divide((long) 1, (long) 3));
    }

    public void testPercentage()
    {
        assertEquals(33, mb.getPercentage((long) 1, (long) 3));
    }
}
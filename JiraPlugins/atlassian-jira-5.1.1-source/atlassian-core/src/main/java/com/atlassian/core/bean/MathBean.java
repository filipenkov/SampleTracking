/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Jan 30, 2003
 * Time: 10:29:22 AM
 * CVS Revision: $Revision: 1.7 $
 * Last CVS Commit: $Date: 2004/01/22 01:15:21 $
 * Author of last CVS Commit: $Author: dloeng $
 * To change this template use Options | File Templates.
 */
package com.atlassian.core.bean;

public class MathBean
{
    public int getPercentageWidth(int numberOfColumns, int column)
    {
        int columnWidth = divide(100, numberOfColumns);
        if (numberOfColumns == column)
            return subtract(100, multiply(columnWidth, subtract(numberOfColumns, 1)));
        else
            return columnWidth;
    }

    public long getPercentage(double portion, double total)
    {

        long columnWidth = Math.round((portion / total) * 100);
        return columnWidth;
    }

    public long getPercentage(long portion, long total)
    {
        long columnWidth = Math.round(((double) portion / (double) total) * 100);
        return columnWidth;
    }

    public int add(int i1, int i2)
    {
        return i1 + i2;
    }

    public int subtract(int i1, int i2)
    {
        return i1 - i2;
    }

    public long substract(long i1, long i2)
    {
        return i1 - i2;
    }

    public int multiply(int i1, int i2)
    {
        return i1 * i2;
    }

    public int divide(int i1, int i2)
    {
        return i1 / i2;
    }

    public long divide(long l1, long l2)
    {
        return l1 / l2;
    }

    public long max(long l1, long l2)
    {
        return Math.max(l1, l2);
    }

    public long min(long l1, long l2)
    {
        return Math.min(l1, l2);
    }
}

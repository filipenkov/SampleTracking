/*
 * Atlassian Source Code Template.
 * User: owen
 * Date: Oct 15, 2002
 * Time: 1:44:24 PM
 * CVS Revision: $Revision: 1.5 $
 * Last CVS Commit: $Date: 2003/09/30 07:05:53 $
 * Author of last CVS Commit: $Author: mcannon $
 */
package com.atlassian.core.util;

import com.atlassian.core.util.PropertyUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

public class TestPropertyUtils extends TestCase
{
    public TestPropertyUtils(String s)
    {
        super(s);
    }

    public void testIdenticalWithNulls()
    {
        //Check to see if the first Property set is null false is returned
        assertTrue(!PropertyUtils.identical(null, PropertySetManager.getInstance("memory", null)));
        //Check to see if the second Property set is null false is returned
        assertTrue(!PropertyUtils.identical(PropertySetManager.getInstance("memory", null), null));
        //Check to see if the both Property set is null true is returned
        assertTrue(PropertyUtils.identical(null, null));
    }

    public void testIdenticalWithDifferentKeys()
    {
        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setString("TEST_KEY_1", "");

        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setString("TEST_KEY_2", "");

        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));
    }

    public void testIdenticalWithBooleans()
    {
        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setBoolean("TEST_BOOLEAN", true);

        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setBoolean("TEST_BOOLEAN", false);

        //Ensure that it returns false when the booleans are different
        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));

        propertySet2.setBoolean("TEST_BOOLEAN", true);

        //Ensure that it returns true when the booleans are the same
        assertTrue(PropertyUtils.identical(propertySet1, propertySet2));
    }

    public void testIdenticalWithDates()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 1, 1, 1, 1, 1);
        Date testDate1 = cal.getTime();

        cal.set(2001, 1, 1, 1, 1, 1);
        Date testDate2 = cal.getTime();

        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setDate("TEST_DATE", testDate1);

        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setDate("TEST_DATE", testDate2);

        //Ensure that it returns false when the dates are different
        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));

        cal.set(2000, 1, 1, 1, 1, 1);
        testDate2 = cal.getTime();

        propertySet2.setDate("TEST_DATE", testDate2);

        //Ensure that is return true when the dates are the same
        assertTrue(PropertyUtils.identical(propertySet1, propertySet2));
    }

    public void testIdenticalWithDoubles()
    {
        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setDouble("TEST_DOUBLE", 10);

        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setDouble("TEST_DOUBLE", 11);

        //Ensure that it returns false when the doubles are different
        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));

        propertySet2.setDouble("TEST_DOUBLE", 10);

        //Ensure that it returns true when the doubles are the same
        assertTrue(PropertyUtils.identical(propertySet1, propertySet2));
    }

    public void testIdenticalWithInts()
    {
        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setInt("TEST_INT", 10);

        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setInt("TEST_INT", 11);

        //Ensure that it returns false when the ints are different
        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));

        propertySet2.setInt("TEST_INT", 10);

        //Ensure that it returns true when the ints are the same
        assertTrue(PropertyUtils.identical(propertySet1, propertySet2));
    }

    public void testIdenticalWithLongs()
    {
        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setLong("TEST_LONG", 10);

        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setLong("TEST_LONG", 11);

        //Ensure that it returns false when the longs are different
        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));

        propertySet2.setLong("TEST_LONG", 10);

        //Ensure that it returns true when the longs are the same
        assertTrue(PropertyUtils.identical(propertySet1, propertySet2));
    }

    public void testIdenticalWithStrings()
    {
        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setString("TEST_STRING", "STRING1");

        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setString("TEST_STRING", "STRING2");

        //Ensure that it returns false when the strings are different
        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));

        propertySet2.setString("TEST_STRING", "STRING1");

        //Ensure that it returns true when the strings are the same
        assertTrue(PropertyUtils.identical(propertySet1, propertySet2));
    }

    public void testIdenticalWithTexts()
    {
        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setText("TEST_TEXT", "SOME TEXT 1");

        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setText("TEST_TEXT", "SOME TEXT 2");

        //Ensure that it returns false when the texts are different
        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));

        propertySet2.setText("TEST_TEXT", "SOME TEXT 1");

        //Ensure that it returns true when the texts are the same
        assertTrue(PropertyUtils.identical(propertySet1, propertySet2));
    }

    public void testIdenticalWithAllMultipleValues()
    {
        PropertySet propertySet1 = PropertySetManager.getInstance("memory", null);
        propertySet1.setBoolean("TEST_BOOLEAN", true);
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 1, 1, 1, 1, 1);
        Date testDate1 = cal.getTime();
        propertySet1.setDate("TEST_DATE", testDate1);
        propertySet1.setDouble("TEST_DOUBLE", 10);
        propertySet1.setInt("TEST_INT", 10);
        propertySet1.setLong("TEST_LONG", 10);
        propertySet1.setString("TEST_STRING", "STRING1");
        propertySet1.setText("TEST_TEXT", "SOME TEXT 1");


        PropertySet propertySet2 = PropertySetManager.getInstance("memory", null);
        propertySet2.setBoolean("TEST_BOOLEAN", true);
        cal.set(2000, 1, 1, 1, 1, 1);
        Date testDate2 = cal.getTime();
        propertySet2.setDate("TEST_DATE", testDate2);
        propertySet2.setDouble("TEST_DOUBLE", 10);
        propertySet2.setInt("TEST_INT", 10);
        propertySet2.setLong("TEST_LONG", 10);
        propertySet2.setString("TEST_STRING", "STRING1");
        propertySet2.setText("TEST_TEXT", "SOME TEXT 2");

        //Ensure that it returns false when the text are different
        assertTrue(!PropertyUtils.identical(propertySet1, propertySet2));

        propertySet2.setText("TEST_TEXT", "SOME TEXT 1");

        //Ensure that it returns true when the booleans are the same
        assertTrue(PropertyUtils.identical(propertySet1, propertySet2));
    }
}

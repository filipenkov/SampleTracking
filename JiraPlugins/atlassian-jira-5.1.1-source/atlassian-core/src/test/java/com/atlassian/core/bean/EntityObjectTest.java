package com.atlassian.core.bean;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public class EntityObjectTest extends TestCase
{
    public EntityObjectTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testEquals()
    {
        EntityObject a = new EntityObject();
        EntityObject b = new EntityObject();

        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.hashCode() ==  a.hashCode());

        Assert.assertTrue(a.equals(b)); // both have id == 0.

        a.setId(1);
        b.setId(1);

        Assert.assertTrue(a.equals(b));
        Assert.assertTrue(a.hashCode() == b.hashCode());

        b.setId(2);

        Assert.assertFalse(a.equals(b));

        Assert.assertFalse(a.equals(null));

    }
}

package com.atlassian.core.util.filter;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TestListFilter extends TestCase
{
    private List testList = Arrays.asList(new String[]{"a", "b", null, "c"});

    public void testFilter()
    {
        testFilter("a", new String[]{"b", null, "c"});
        testFilter("b", new String[]{"a", null, "c"});
        testFilter(null, new String[]{"a", "b", "c"});
        testFilter("c", new String[]{"a", "b", null});
    }

    public void testRemove()
    {
        List myList = new ArrayList(testList);
        Filter filter = new Filter()
        {
            public boolean isIncluded(Object o)
            {
                return o == null || !o.equals("b");
            }
        };

        String[] expected = new String[]{"a", null, "c"};

        Iterator it = new ListFilter(filter).filterIterator(myList.iterator());
        int i = 0;
        while (it.hasNext())
        {
            Assert.assertEquals(expected[i++], it.next());
            it.remove();
        }

        Assert.assertEquals(1, myList.size());
        Assert.assertEquals("b", myList.get(0));
    }

    private void testFilter(String filterOut, String[] expected)
    {
        final String fo = filterOut;
        Filter filter = new Filter()
        {
            public boolean isIncluded(Object o)
            {
                if (o == null)
                {
                    return o != fo;
                }

                return !o.equals(fo);
            }
        };

        Assert.assertEquals(Arrays.asList(expected), new ListFilter(filter).filterList(testList));

        Iterator it = new ListFilter(filter).filterIterator(testList.iterator());
        int i = 0;
        while (it.hasNext())
        {
            // make sure we can keep calling it.hasNext()
            Assert.assertTrue(it.hasNext());
            Assert.assertEquals(expected[i++], it.next());
        }

        // check we can call hasNext again after the end of the list
        Assert.assertFalse(it.hasNext());
        Assert.assertEquals("correct number in iterator", expected.length, i);
    }
}

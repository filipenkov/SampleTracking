package com.atlassian.core.util.collection;

import com.atlassian.core.util.collection.EasyList;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class TestEasyList extends TestCase
{
    String[] letters = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l" };

    public void testMergeListsWithItems()
    {

        List[] a = new List[3];

        // create three lists of four items
        int j = 0;
        a[0] = EasyList.createList(4);
        for (int i = 0; i < letters.length; i++)
        {
            a[j].add(letters[i]);
            if ((((i + 1) % 4) == 0) && (j < 2))
            {
                j++;
                a[j] = EasyList.createList(4);
            }
        }

        List d = EasyList.mergeLists(a[0], a[1], a[2]);

        // check they are all there
        int i = 0;
        for (Iterator iter = d.iterator(); iter.hasNext(); i++)
        {
            String listLetter = (String) iter.next();
            assertEquals(letters[i], listLetter);
        }
    }

    public void testMergeListWithAllNulls()
    {
        List[] a = new List[3];
        a[0] = null;
        a[1] = null;
        a[2] = null;
        List d = EasyList.mergeLists(a[0], a[1], a[2]);

        assertEquals(d.size(), 0);
    }

    public void testMergeListOneNull()
    {
        List[] a = new List[3];
        a[0] = EasyList.build("a");
        a[1] = EasyList.build("b");
        a[2] = null;
        List d = EasyList.mergeLists(a[0], a[1], a[2]);

        assertEquals(d.size(), 2);

        int i = 0;
        for (Iterator iter = d.iterator(); iter.hasNext(); i++)
        {
            String listLetter = (String) iter.next();
            assertEquals(letters[i], listLetter);
        }

    }

    public void testMergeListTwoNulls()
    {
        List[] a = new List[3];
        a[0] = null;
        a[1] = EasyList.build("a", "b", "c");
        a[2] = null;
        List d = EasyList.mergeLists(a[0], a[1], a[2]);

        assertEquals(d.size(), 3);

        int i = 0;
        for (Iterator iter = d.iterator(); iter.hasNext(); i++)
        {
            String listLetter = (String) iter.next();
            assertEquals(letters[i], listLetter);
        }
    }

    public void testSplitListWithNull()
    {
        List input = null;
        try
        {
            EasyList.shallowSplit(input, 5);
            fail("Split method should have thrown exception");
        }
        catch (NullPointerException yay)
        {}
    }

    public void testSplitListWithSmallList()
    {
        List input = new ArrayList();
        input.add("1");
        input.add("2");
        input.add("3");
        input.add("4");
        try
        {
            List res = EasyList.shallowSplit(input, 5);
            assertNotNull(res);
            assertEquals("Length", 1, res.size());
            assertEquals("LengthSub", 4, ((List) res.get(0)).size());
        }
        catch (Exception e)
        {
            fail("Split method throw exception: " + e);
        }
    }

    public void testSplitListWithEqualListSizeAndSublength()
    {
        List input = new ArrayList();
        input.add("1");
        input.add("2");
        input.add("3");
        input.add("4");
        input.add("5");
        try
        {
            List res = EasyList.shallowSplit(input, 5);
            assertNotNull(res);
            assertEquals("Length", 1, res.size());
            assertEquals("LengthSub", 5, ((List) res.get(0)).size());
        }
        catch (Exception e)
        {
            fail("Split method throw exception: " + e);
        }
    }

    public void testSplitListWithLargeList()
    {
        List input = new ArrayList();
        input.add("1");
        input.add("2");
        input.add("3");
        input.add("4");
        input.add("5");
        input.add("6");
        try
        {
            List res = EasyList.shallowSplit(input, 5);
            assertNotNull(res);
            assertEquals("Length", 2, res.size());
            assertEquals("LengthSub", 5, ((List) res.get(0)).size());
            assertEquals("LengthSub2", 1, ((List) res.get(1)).size());
        }
        catch (Exception e)
        {
            fail("Split method throw exception: " + e);
        }
    }

    public void testSplitWithSameSizeList()
    {
        List input = new ArrayList();
        input.add("1");
        input.add("2");
        input.add("3");
        input.add("4");
        try
        {
            List res = EasyList.shallowSplit(input, 2);
            assertNotNull(res);
            assertEquals("Length", 2, res.size());
            assertEquals("LengthSub", 2, ((List) res.get(0)).size());
            assertEquals("LengthSub2", 2, ((List) res.get(1)).size());
        }
        catch (Exception e)
        {
            fail("Split method throw exception: " + e);
        }
    }

    public void testBuild1()
    {
        List list = EasyList.build("A");
        assertEquals(1, list.size());
        assertEquals("A", list.get(0));
    }

    public void testBuild2()
    {
        List list = EasyList.build("A", "B");
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    public void testBuild3()
    {
        List list = EasyList.build("A", "B", "C");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    public void testBuild4()
    {
        List list = EasyList.build("A", "B", "C", "D");
        assertEquals(4, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
    }

    public void testBuild5()
    {
        List list = EasyList.build("A", "B", "C", "D", "E");
        assertEquals(5, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
        assertEquals("E", list.get(4));
    }

    public void testBuild6()
    {
        List list = EasyList.build("A", "B", "C", "D", "E", "F");
        assertEquals(6, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
        assertEquals("E", list.get(4));
        assertEquals("F", list.get(5));
    }

    public void testBuild7()
    {
        List list = EasyList.build("A", "B", "C", "D", "E", "F", "G");
        assertEquals(7, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
        assertEquals("E", list.get(4));
        assertEquals("F", list.get(5));
        assertEquals("G", list.get(6));
    }

    public void testBuild8()
    {
        List list = EasyList.build("A", "B", "C", "D", "E", "F", "G", "H");
        assertEquals(8, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
        assertEquals("E", list.get(4));
        assertEquals("F", list.get(5));
        assertEquals("G", list.get(6));
        assertEquals("H", list.get(7));
    }
}
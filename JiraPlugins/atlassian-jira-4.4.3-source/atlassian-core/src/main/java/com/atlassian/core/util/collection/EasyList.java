package com.atlassian.core.util.collection;

import com.atlassian.core.util.ObjectUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A replacement for UtilMisc.toList(). <p/> Most methods here are not null safe
 */
public class EasyList
{

    /**
     * Creates a list with one null value. Occasionally useful.
     * @return a list with one null value.
     */
    public static List buildNull()
    {
        List list = createList(1);
        list.add(null);
        return list;
    }

    public static List build(Object[] array)
    {
        List list = createList(array.length);

        for (int i = 0; i < array.length; i++)
        {
            Object o = array[i];
            list.add(o);
        }

        return list;
    }

    public static List buildNonNull(Object[] array)
    {
        List list;
        if (array != null && array.length > 0)
        {
            list = createList(array.length);
            for (int i = 0; i < array.length; i++)
            {
                Object o = array[i];
                if (ObjectUtils.isNotEmpty(o))
                {
                    list.add(o);
                }
            }
        }
        else
        {
            list = Collections.EMPTY_LIST;
        }

        return list;
    }

    public static List buildNonNull(Collection c)
    {
        List list;
        if (c != null && !c.isEmpty())
        {
            list = build(CollectionUtils.predicatedCollection(c, ObjectUtils.getIsSetPredicate()));
        }
        else
        {
            list = Collections.EMPTY_LIST;
        }

        return list;
    }

    public static List buildNonNull(Object o)
    {
        if (ObjectUtils.isNotEmpty(o))
        {
            return build(o);
        }
        else
        {
            return build();
        }
    }

    public static List build()
    {
        return Collections.EMPTY_LIST;
    }

    public static List build(Object o1)
    {
        List list = createList(1);

        list.add(o1);

        return list;
    }

    public static List build(Object o1, Object o2)
    {
        List list = createList(2);

        list.add(o1);
        list.add(o2);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3)
    {
        List list = createList(3);

        list.add(o1);
        list.add(o2);
        list.add(o3);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4)
    {
        List list = createList(4);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5)
    {
        List list = createList(5);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6)
    {
        List list = createList(6);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7)
    {
        List list = createList(7);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8)
    {
        List list = createList(8);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);
        list.add(o8);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
    {
        List list = createList(9);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);
        list.add(o8);
        list.add(o9);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10)
    {
        List list = createList(10);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);
        list.add(o8);
        list.add(o9);
        list.add(o10);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10, Object o11)
    {
        List list = createList(11);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);
        list.add(o8);
        list.add(o9);
        list.add(o10);
        list.add(o11);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
        Object o11, Object o12)
    {
        List list = createList(12);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);
        list.add(o8);
        list.add(o9);
        list.add(o10);
        list.add(o11);
        list.add(o12);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
        Object o11, Object o12, Object o13)
    {
        List list = createList(13);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);
        list.add(o8);
        list.add(o9);
        list.add(o10);
        list.add(o11);
        list.add(o12);
        list.add(o13);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
        Object o11, Object o12, Object o13, Object o14)
    {
        List list = createList(14);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);
        list.add(o8);
        list.add(o9);
        list.add(o10);
        list.add(o11);
        list.add(o12);
        list.add(o13);
        list.add(o14);

        return list;
    }

    public static List build(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10,
        Object o11, Object o12, Object o13, Object o14, Object o15, Object o16, Object o17, Object o18, Object o19, Object o20, Object o21,
        Object o22, Object o23, Object o24, Object o25, Object o26)
    {
        List list = createList(26);

        list.add(o1);
        list.add(o2);
        list.add(o3);
        list.add(o4);
        list.add(o5);
        list.add(o6);
        list.add(o7);
        list.add(o8);
        list.add(o9);
        list.add(o10);
        list.add(o11);
        list.add(o12);
        list.add(o13);
        list.add(o14);
        list.add(o15);
        list.add(o16);
        list.add(o17);
        list.add(o18);
        list.add(o19);
        list.add(o20);
        list.add(o21);
        list.add(o22);
        list.add(o23);
        list.add(o24);
        list.add(o25);
        list.add(o26);

        return list;
    }

    public static List build(Collection collection)
    {
        if (collection == null) return null;
        return new ArrayList(collection);
    }

    public static List createList(int size)
    {
        return new ArrayList(size);
    }

    /**
     * Merge a maximum of three lists.
     * Null lists passed in will be ignored.
     *
     * @param a The first list.
     * @param b The second list.
     * @param c The third list.
     * @return A merged list containing the objects of all three passed in lists.
     */
    public static List mergeLists(List a, List b, List c)
    {
        List d = EasyList.createList(0);
        if (a != null)
        {
            d.addAll(a);
        }
        if (b != null)
        {
            d.addAll(b);
        }
        if (c != null)
        {
            d.addAll(c);
        }
        return d;
    }

    /**
     * Splits a list into a number of sublists of the correct length. Note this will create a 'shallow' split, in other
     * words if you set/remove on the sublists, this will modify the parent list as well (same vice versa). Therefore,
     * DO NOT publish the result of this method to clients as the you will be publishing read/write access to your
     * underlying data and the results will be unpredictable, especially in a multi-threaded context.
     * 
     * @param list
     *            The list to split
     * @param sublength
     *            Length of the sublists
     * @return A list of lists of the correct length
     */
    public static List shallowSplit(List list, int sublength)
    {
        // if we have any remainder, then we will add one to our division to correctly size the result list
        int overflow = ((list.size() % sublength) > 0) ? 1 : 0;
        List result = new ArrayList((list.size() / sublength) + overflow);
        int i = 0;
        while (i < list.size())
        {
            int endIndex = (i + sublength) > list.size() ? list.size() : i + sublength;
            result.add(list.subList(i, endIndex));
            i += sublength;
        }

        return result;
    }
}

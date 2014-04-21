/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.*;

public class TestPagerFilter extends LegacyJiraMockTestCase
{
    public TestPagerFilter(String s)
    {
        super(s);
    }

    public void testGetCurrentPageArrayList()
    {
        List<Long> items = new ArrayList<Long>();
        for (int i = 0; i < 40; i++)
        {
            items.add(new Long(i));
        }

        runGetCurrentPage(items);
        runGetCurrentPageStartOverMax(items);
    }

    private void runGetCurrentPage(List<Long> items)
    {
        PagerFilter<Long> pf = new PagerFilter<Long>();

        Collection<Long> results = pf.getCurrentPage(items);
        assertNotNull(results);
        assertTrue(!results.isEmpty());
        assertEquals(20, results.size());

        List<Long> testList = new ArrayList<Long>(results);
        for (int j = 0; j < 20; j++)
        {
            assertEquals(new Long(j), testList.get(j));
        }
    }

    public void testGetCurrentPageEmpty()
    {
        PagerFilter<String> pf = new PagerFilter<String>();

        pf.setStart(100);

        Collection<String> results = pf.getCurrentPage(Collections.<String>emptyList());
        assertNotNull(results);
        assertTrue(results.isEmpty());
        assertEquals(0, pf.getStart());
    }

    public void testGetCurrentPageNull()
    {
        // assert it acts like an empty list
        PagerFilter<String> pf = new PagerFilter<String>();

        pf.setStart(100);

        Collection<String> results = pf.getCurrentPage(null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        assertEquals(0, pf.getStart());
    }

    private void runGetCurrentPageStartOverMax(List<Long> items)
    {
        PagerFilter<Long> pf = new PagerFilter<Long>();

        pf.setStart(100);

        Collection<Long> results = pf.getCurrentPage(items);
        assertNotNull(results);
        assertTrue(!results.isEmpty());
        assertEquals(20, results.size());

        List<Long> testList = new ArrayList<Long>(results);
        for (int j = 0; j < 20; j++)
        {
            assertEquals(new Long(j), testList.get(j));
        }
        assertEquals(0, pf.getStart());
    }

    public void testGettersSetters()
    {
        PagerFilter pf = new PagerFilter();

        assertEquals(20, pf.getMax());
        pf.setMax(10);
        assertEquals(10, pf.getMax());

        assertEquals(0, pf.getStart());
        pf.setStart(15);
        assertEquals(15, pf.getStart());

        assertEquals(25, pf.getEnd());
        assertEquals(25, pf.getNextStart());
        assertEquals(5, pf.getPreviousStart());

        pf.setStart(5);
        assertEquals(0, pf.getPreviousStart());
    }

}

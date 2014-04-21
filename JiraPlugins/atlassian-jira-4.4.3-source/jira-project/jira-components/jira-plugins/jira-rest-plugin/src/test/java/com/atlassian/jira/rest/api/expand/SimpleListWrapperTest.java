package com.atlassian.jira.rest.api.expand;

import com.atlassian.plugins.rest.common.expand.entity.ListWrapperCallback;
import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;

/**
 * Unit tests for SLWrapper.
 */
public class SimpleListWrapperTest extends TestCase
{
    public void testListWrapperWithNullList()
    {
        try
        {
            SimpleListWrapper.ofList(null, 10);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            // success
        }
    }

    public void testListWrapperWithNegativeMaxResults()
    {
        List<Integer> list = Arrays.asList(1, 2);
        Indexes indexes = createMock(Indexes.class);
        expect(indexes.getIndexes(list.size())).andReturn(new TreeSet<Integer>(list));

        try
        {
            ListWrapperCallback<Integer> cb = SimpleListWrapper.ofList(list, -1);
            fail("Expected an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }

    public void testListWrapperWithZeroMaxResults()
    {
        List<Integer> list = Arrays.asList(1, 2);
        Indexes indexes = createMock(Indexes.class);
        expect(indexes.getIndexes(list.size())).andReturn(new TreeSet<Integer>(list));

        ListWrapperCallback<Integer> cb = SimpleListWrapper.ofList(list, 0);
        List<Integer> items = cb.getItems(indexes);
        assertEquals(0, items.size());
    }
}

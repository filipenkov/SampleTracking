package com.atlassian.jira.util.collect;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.Supplier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TestMultiMaps extends ListeningTestCase
{
    @Test
    public void testCreator() throws Exception
    {
        final MultiMap<String, String, Set<String>> map = MultiMaps.create(new HashMap<String, Set<String>>(), new Supplier<Set<String>>()
        {
            public Set<String> get()
            {
                return new HashSet<String>();
            }
        });

        map.put("one", "one");
        map.put("one", "two");
        map.put("one", "three");
        map.put("one", "four");
        map.put("one", "two");

        map.put("two", "one");
        map.put("two", "two");
        map.put("two", "two");

        assertEquals(2, map.size());
        assertEquals(4, map.allValues().size());
        assertEquals(6, map.sizeAll());

        assertTrue(map.contains("one"));
        assertTrue(map.contains("two"));
        assertTrue(map.contains("three"));
        assertTrue(map.contains("four"));
        assertFalse(map.contains("five"));

        assertTrue(map.containsValue(new HashSet<String>(EasyList.build("one", "two"))));
        assertFalse(map.containsValue(new HashSet<String>(EasyList.build("one", "two", "three"))));
        assertTrue(map.containsValue(new HashSet<String>(EasyList.build("one", "two", "three", "four"))));

        assertEquals(4, map.get("one").size());
        assertEquals(2, map.get("two").size());
    }
}

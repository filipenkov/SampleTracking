package com.atlassian.jira.util.cache;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.lang.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Test case for {@link com.atlassian.jira.util.cache.GenericCompositeKeyCache}.
 *
 * @since v4.2
 */
public class TestGenericCompositeCache extends ListeningTestCase
{
    private static NotInternedString newString(String s)
    {
        return new NotInternedString(s);
    }

    private static class NotInternedString
    {
        private String string;
        NotInternedString(String string)
        {
            this.string = string;
        }

        @Override
        public String toString()
        {
            return "Not interned [" + string + "]";
        }
    }

    private static Map<Pair<String,String>, List<NotInternedString>> newResultMap()
    {
        return new ConcurrentHashMap<Pair<String,String>, List<NotInternedString>>();
    }


    private static final int NO_THREADS = 10;
    private static final int NO_RUNS = 100;
    private static final List<Pair<String,String>> TEST_KEYS = Arrays.asList(
        Pair.of("first1", "first2"),
        Pair.of("second1", "second2")
    );

    private GenericCompositeKeyCache<String,String,NotInternedString> tested;
    private final ExecutorService executor = Executors.newFixedThreadPool(NO_THREADS);
    private final Map<Pair<String,String>, List<NotInternedString>> expectedResults = newResultMap();

    private final CountDownLatch startGate = new CountDownLatch(1);
    private final CountDownLatch endGate = new CountDownLatch(NO_RUNS);


    private class TestRunner implements Callable<NotInternedString>
    {
        private String first;
        private String second;

        TestRunner(String first, String second)
        {
            this.first = first;
            this.second = second;            
        }

        public NotInternedString call() throws Exception
        {
            try {
                startGate.await();
                return tested.get(first, second);
            } finally {
                endGate.countDown();
            }
        }
    }

    @Before
    public void setUp() throws Exception
    {
        tested = new GenericCompositeKeyCache<String,String,NotInternedString>(new Function<Pair<String,String>, NotInternedString>()
        {
            public NotInternedString get(final Pair<String, String> input)
            {
                NotInternedString value = newString(String.format("VALUE[%s][%s]", input.first(), input.second()));
                expectedResults.get(input).add(value);
                return value;
            }
        });
        setUpExpectedResults(TEST_KEYS);
    }

    private void setUpExpectedResults(List<Pair<String,String>> testKeys)
    {
        for (Pair<String,String> key : testKeys)
        {
            expectedResults.put(key, new CopyOnWriteArrayList<NotInternedString>());
        }
    }

    @Test
    public void testCacheConsistency() throws Exception
    {
        List<Future<NotInternedString>> firstValues = new ArrayList<Future<NotInternedString>>();
        List<Future<NotInternedString>> secondValues = new ArrayList<Future<NotInternedString>>();
        for (int i=0; i<50; i++)
        {
            firstValues.add(executor.submit(new TestRunner("first1", "first2")));
            secondValues.add(executor.submit(new TestRunner("second1", "second2")));
        }
        startGate.countDown();
        endGate.await();
        assertResults(firstValues, Pair.of("first1", "first2"));
        assertResults(secondValues, Pair.of("second1", "second2"));
    }

    private void assertResults(final List<Future<NotInternedString>> values, Pair<String,String> key) throws Exception
    {
        assertEquals(50, values.size());
        NotInternedString expectedFirst = findExpectedCachedValue(values, key);
        assertNotNull(expectedFirst);
        for (Future<NotInternedString> result : values)
        {
            assertSame(expectedFirst, result.get());
        }
    }

    private NotInternedString findExpectedCachedValue(List<Future<NotInternedString>> results, Pair<String,String> key)
            throws Exception
    {
        assertFalse(results.isEmpty());
        NotInternedString firstResult = results.get(0).get();
        assertTrue(expectedResults.get(key).contains(firstResult));
        return firstResult;
    }


}

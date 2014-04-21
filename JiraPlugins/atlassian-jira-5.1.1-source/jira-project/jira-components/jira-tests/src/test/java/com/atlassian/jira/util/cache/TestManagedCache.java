package com.atlassian.jira.util.cache;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.concurrent.Gate;
import com.google.common.base.Function;
import com.atlassian.jira.local.ListeningTestCase;

import javax.annotation.Nullable;

/**
 *
 * @since v4.2
 */
public class TestManagedCache extends ListeningTestCase
{
    private Function<Long, String> longStringfactory = new Function<Long, String>()
    {
        public String apply(@Nullable final Long l)
        {
            if (l == 0)
            {
                return null;
            }
            if (l == -1)
            {
                throw new RuntimeException();
            }
            return "x" + l.toString();
        }
    };

    @Test
    public void testSimple()
    {
        ManagedCache<Long, String> cache = ManagedCache.newManagedCache(longStringfactory);

        String a = cache.get(10L);
        assertEquals("x10", a);
        assertEquals(a, cache.get(10L));
        assertTrue(a == cache.get(10L)); // should be same instance

        assertEquals(1, cache.size());

        cache.remove(10L);

        assertEquals(0, cache.size());

        String b = cache.get(10L);
        assertEquals("x10", b);
        assertEquals(b, cache.get(10L));
        assertTrue(b == cache.get(10L)); // should be same instance
        assertTrue(b != a); // different instance to original one

        assertEquals(1, cache.size());
    }

    @Test
    public void testNulls()
    {

        ManagedCache<Long, String> cache = ManagedCache.newManagedCache(longStringfactory);

        assertNull(cache.get(0L));
        assertEquals(0, cache.size());
    }

    @Test
    public void testConcurrentRemoveDuringGet() throws Exception
    {

        final Gate inFactory = new Gate(1);
        final Gate removeDone = new Gate(1);
        final Gate removerStarted = new Gate(1);

        final ManagedCache<Long, String> cache = ManagedCache.newManagedCache(new Function<Long, String>()
        {
            public String apply(@Nullable final Long l)
            {
                inFactory.go();
                removeDone.ready();
                return "x" + l;
            }
        });

        Thread remover = new Thread(new Runnable()
        {
            public void run()
            {
                removerStarted.ready();

                inFactory.ready();
                cache.remove(10L);
                removeDone.go();
            }
        });

        remover.start();
        removerStarted.go();

        // call get(), and during call to factory, allow remover to remove()
        cache.get(10L);

        remover.join();

        assertEquals(0, cache.size());
    }
}

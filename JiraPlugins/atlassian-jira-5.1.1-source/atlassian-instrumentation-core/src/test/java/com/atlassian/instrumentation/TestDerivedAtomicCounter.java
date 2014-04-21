package com.atlassian.instrumentation;

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @since v4.0
 */
public class TestDerivedAtomicCounter extends TestCase
{
    public void testConstructor()
    {
        DerivedCounter derivedCounter = new DerivedAtomicCounter("name", 666);
        assertEquals("name", derivedCounter.getName());
        assertEquals(666, derivedCounter.getValue());

        derivedCounter = new DerivedAtomicCounter("name");
        assertEquals("name", derivedCounter.getName());
        assertEquals(0, derivedCounter.getValue());

        try
        {
            new DerivedAtomicCounter(null);
            fail("Should have barfed on null name");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new DerivedAtomicCounter(null, 666);
            fail("Should have barfed on null name");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new DerivedAtomicCounter("name", null);
            fail("Should have barfed on null long reference");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    public void testChanges()
    {
        DerivedCounter derivedCounter = new DerivedAtomicCounter("name", 666);
        assertEquals(666, derivedCounter.getValue());
        assertEquals(667, derivedCounter.incrementAndGet());
        assertEquals(667, derivedCounter.getValue());
        assertEquals(670, derivedCounter.addAndGet(3));
        assertEquals(650, derivedCounter.addAndGet(-20));
        assertEquals(670, derivedCounter.addAndGet(20));
        assertEquals(669, derivedCounter.decrementAndGet());
    }

    public void testWithReferenceToOutsideAtomicLong()
    {
        AtomicLong outsideLong = new AtomicLong(666);

        DerivedCounter derivedCounter = new DerivedAtomicCounter("name", outsideLong);
        assertEquals("name", derivedCounter.getName());
        assertEquals(666, derivedCounter.getValue());

        outsideLong.incrementAndGet();
        assertEquals(667, derivedCounter.getValue());

    }
}
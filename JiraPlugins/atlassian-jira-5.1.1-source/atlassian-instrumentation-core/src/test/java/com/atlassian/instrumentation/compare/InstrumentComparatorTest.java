package com.atlassian.instrumentation.compare;

import com.atlassian.instrumentation.AtomicCounter;
import com.atlassian.instrumentation.Instrument;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InstrumentComparatorTest extends TestCase
{
    AtomicCounter i1 = new AtomicCounter("zebra", 456);
    AtomicCounter i2 = new AtomicCounter("zebra", 666);
    AtomicCounter i3 = new AtomicCounter("aarddvark", 23);
    AtomicCounter i4 = new AtomicCounter("zebra", 23);
    AtomicCounter i5 = new AtomicCounter("dog", 23);

    private final List<Instrument> rawList = new ArrayList<Instrument>(Arrays.asList(i1, i2, i3, i4, i5));

    public void testInstrumentComparator()
    {
        List<Instrument> expectedList = new ArrayList<Instrument>(Arrays.asList(i2, i1, i3, i5, i4));

        InstrumentComparator comparator = new InstrumentComparator();


        List<Instrument> actualList = new ArrayList<Instrument>(rawList);
        Collections.sort(actualList, comparator);

        assertInstrumentsEqual(expectedList, actualList);
    }

    public void testInstrumentNameComparator()
    {
        List<Instrument> expectedList = new ArrayList<Instrument>(Arrays.asList(i3, i5, i2, i1, i4));

        InstrumentNameComparator comparator = new InstrumentNameComparator();


        List<Instrument> actualList = new ArrayList<Instrument>(rawList);
        Collections.sort(actualList, comparator);

        assertInstrumentsEqual(expectedList, actualList);
    }

    private void assertInstrumentsEqual(final List<Instrument> expectedList, final List<Instrument> actualList)
    {
        assertEquals(expectedList.size(), actualList.size());
        int i = 0;
        for (Instrument expected : expectedList)
        {
            Instrument actual = actualList.get(i);
            assertSame(expected, actual);
            i++;
        }
    }

}

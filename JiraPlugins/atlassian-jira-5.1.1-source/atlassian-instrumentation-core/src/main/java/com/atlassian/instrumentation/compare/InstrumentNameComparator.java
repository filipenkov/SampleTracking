package com.atlassian.instrumentation.compare;

import com.atlassian.instrumentation.Instrument;

import java.util.Comparator;

/**
 * A {@link java.util.Comparator} for sorting {@link com.atlassian.instrumentation.Instrument}s by name
 * <p/>
 * It sorts in lowest name value / highest value.
 *
 * @since v4.0
 */
public class InstrumentNameComparator implements Comparator<Instrument>
{
    public int compare(final Instrument o1, final Instrument o2)
    {
        if (o1 == o2)
        {
            return 0;
        }
        if (o1 == null && o2 != null)
        {
            return -1;
        }
        if (o1 != null && o2 == null)
        {
            return 1;
        }
        long rc = o1.getName().compareTo(o2.getName());
        if (rc == 0)
        {
            rc = o2.getValue() - o1.getValue();
        }
        return rc > 0 ? 1 : rc < 0 ? -1 : 0;
    }
}
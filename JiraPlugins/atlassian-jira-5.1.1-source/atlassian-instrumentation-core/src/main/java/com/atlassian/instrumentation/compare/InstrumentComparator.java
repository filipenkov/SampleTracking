package com.atlassian.instrumentation.compare;

import com.atlassian.instrumentation.Instrument;

import java.util.Comparator;

/**
 * A {@link java.util.Comparator} for {@link com.atlassian.instrumentation.Instrument}s
 * <p/>
 * It sorts in highest value / lowest name order.
 *
 * @since v4.0
 */
public class InstrumentComparator implements Comparator<Instrument>
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
        long rc = o2.getValue() - o1.getValue();
        if (rc == 0)
        {
            rc = o1.getName().compareTo(o2.getName());
        }
        return rc > 0 ? 1 : rc < 0 ? -1 : 0;
    }
}

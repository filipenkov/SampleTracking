package com.atlassian.instrumentation;

import com.atlassian.instrumentation.utils.dbc.Assertions;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class allows values to be computed on as as needed basis and also cache the values for a certain length of
 * time.
 * <p/>
 * For example imagine a value that is the number of users in a system.  This may be expensive to compute (lots of
 * database calls maybe) but its still fairly accurate if its only done every 1-5 minutes say.
 */
public abstract class CachedExternalValue implements ExternalValue
{
    private final long cacheTimeout;
    private final TimeUnit cacheTimeoutUnits;
    private final AtomicReference<ExpiringLongReference> calculatedValue = new AtomicReference<ExpiringLongReference>();

    /**
     * By default 1 minute is the cache time out value
     */
    protected CachedExternalValue()
    {
        this(60, TimeUnit.SECONDS);    // jdk 1.5 compliant
    }

    /**
     * Creates a CachedExternalValue where the cache time is the values specified.
     *
     * @param cacheTimeout      the cache time out value
     * @param cacheTimeoutUnits the units of the cache time our value
     */
    protected CachedExternalValue(long cacheTimeout, TimeUnit cacheTimeoutUnits)
    {
        Assertions.notNegative("cacheTimeout", cacheTimeout);
        Assertions.notNull("timeUnit", cacheTimeoutUnits);

        this.cacheTimeout = cacheTimeout;
        this.cacheTimeoutUnits = cacheTimeoutUnits;
        calculatedValue.set(new ExpiringLongReference());
    }

    /**
     * This method is called to compute the value of the {@link com.atlassian.instrumentation.ExternalValue}
     * <p/>
     * This will be done because the cached value has expired and has since been requested via a call to {@link
     * #getValue()}
     *
     * @return the computed value
     */
    protected abstract long computeValue();

    public long getValue()
    {
        while (true)
        {
            final ExpiringLongReference value = calculatedValue.get();
            if (!value.isExpired())
            {
                return value.get();
            }
            calculatedValue.compareAndSet(value, new ExpiringLongReference());
        }
    }

    private class ExpiringLongReference extends LazyReference<Long>
    {
        private final Timeout timeout = Timeout.getNanosTimeout(cacheTimeout, cacheTimeoutUnits);


        boolean isExpired()
        {
            return timeout.isExpired();
        }

        protected Long create() throws Exception
        {
            return computeValue();
        }
    }
}

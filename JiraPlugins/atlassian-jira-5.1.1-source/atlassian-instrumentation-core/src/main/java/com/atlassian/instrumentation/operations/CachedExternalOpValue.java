package com.atlassian.instrumentation.operations;

import com.atlassian.instrumentation.utils.dbc.Assertions;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class allows {@link ExternalOpValue}s to be computed on as as needed basis and also cache the values for a certain length of
 * time.
 */
public abstract class CachedExternalOpValue implements ExternalOpValue
{
    private final long cacheTimeout;
    private final TimeUnit cacheTimeoutUnits;
    private final AtomicReference<ExpiringOpSnapshotReference> calculatedValue = new AtomicReference<ExpiringOpSnapshotReference>();

    /**
     * By default 1 minute is the cache time out value
     */
    protected CachedExternalOpValue()
    {
        this(60, TimeUnit.SECONDS);    // jdk 1.5 compliant
    }

    /**
     * Creates a CachedExternalOpValue where the cache time is the values specified.
     *
     * @param cacheTimeout      the cache time out value
     * @param cacheTimeoutUnits the units of the cache time our value
     */
    protected CachedExternalOpValue(long cacheTimeout, TimeUnit cacheTimeoutUnits)
    {
        Assertions.notNegative("cacheTimeout", cacheTimeout);
        Assertions.notNull("timeUnit", cacheTimeoutUnits);

        this.cacheTimeout = cacheTimeout;
        this.cacheTimeoutUnits = cacheTimeoutUnits;
        calculatedValue.set(new ExpiringOpSnapshotReference());
    }

    /**
     * This method is called to compute the value of the {@link CachedExternalOpValue}
     * <p/>
     * This will be done because the cached value has expired and has since been requested via a call to {@link
     * #getSnapshot()}
     *
     * @return the computed value
     */
    protected abstract OpSnapshot computeValue();

    public OpSnapshot getSnapshot()
    {
        while (true)
        {
            final ExpiringOpSnapshotReference value = calculatedValue.get();
            if (!value.isExpired())
            {
                return value.get();
            }
            calculatedValue.compareAndSet(value, new ExpiringOpSnapshotReference());
        }
    }

    private class ExpiringOpSnapshotReference extends LazyReference<OpSnapshot>
    {
        private final Timeout timeout = Timeout.getNanosTimeout(cacheTimeout, cacheTimeoutUnits);

        boolean isExpired()
        {
            return timeout.isExpired();
        }

        protected OpSnapshot create() throws Exception
        {
            return computeValue();
        }
    }
}